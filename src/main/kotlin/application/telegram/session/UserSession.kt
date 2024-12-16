package application.telegram.session

import application.telegram.TelegramLogger
import application.telegram.text_handler.AccountInputHandler
import application.telegram.text_handler.InputTextHandler
import application.telegram.text_handler.LanguageSelectionHandler
import application.telegram.text_handler.TradeConfigInputHandler
import com.antik.utils.arkham.ArkmClient
import com.antik.utils.arkham.createOkHttpClient
import com.antik.utils.use_case.ShowBalanceCase
import com.antik.utils.use_case.StartVolumeTradeCase
import com.antik.utils.use_case.TradeConfig
import com.antik.utils.user.CredentialAccount
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Chat
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import generated.StringKey
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import localization.LocalizationManager
import use_case.FlowCase
import use_case.ShowStatsCase

class UserSession(private val bot: Bot, private val chat: Chat, private val onFinish: (Chat) -> Unit) {

    private val chatId = ChatId.fromId(chat.id)
    private val logger = TelegramLogger(bot, chatId)
    private lateinit var localizationManager: LocalizationManager
    private val languageHandler = LanguageSelectionHandler(bot, chatId)
    private var currentAction: UserAction? = null
    private var inputHandler: InputTextHandler<*>? = null
    private var account: CredentialAccount? = null
    private var runningFlow: FlowCase? = null

    private val scope = CoroutineScope(Dispatchers.Default + CoroutineExceptionHandler { _, throwable ->
        onError(throwable)
    })

    init {
        selectLanguage()
    }

    private fun selectLanguage() {
        languageHandler.showLanguageMenu()
    }

    private fun sendMessage(text: String) {
        bot.sendMessage(
            chatId = chatId,
            text = text,
            parseMode = ParseMode.MARKDOWN
        )
    }

    private fun onStart() {
        if (currentAction == null) {
            sendMessage(localizationManager.getString(StringKey.WELCOME_MESSAGE, chat.firstName))
        }
        resetUserData()
        showActionMenu()
    }

    private fun resetUserData() {
        currentAction = null
        inputHandler = null
        account = null
        runningFlow?.forceStop()
        runningFlow = null
    }

    fun handleUserInput(input: String) {
        val handler = inputHandler
        if (handler != null) {
            handler.handleInput(input)
            if (handler.isComplete()) {
                sendMessage(handler.getCompleteMessage())
                when (handler) {
                    is AccountInputHandler -> {
                        onAccountReceived(handler.getResult()!!)
                    }

                    is TradeConfigInputHandler -> {
                        onTradeConfigReceived(handler.getResult()!!)
                    }
                }
            } else {
                handler.nextStepMessage()?.also {
                    sendMessage(it)
                }
            }
        }
    }

    private fun startInputHandler(handler: InputTextHandler<*>) {
        sendMessage(handler.getStartMessage())
        inputHandler = handler
    }

    private fun finishUserInput() {
        inputHandler = null
    }

    private fun showActionMenu() {
        val inlineKeyboard = InlineKeyboardMarkup.create(
            listOf(
                InlineKeyboardButton.CallbackData(
                    text = localizationManager.getString(StringKey.MENU_ACTION_BALANCE),
                    callbackData = ACTION_SHOW_BALANCE
                ),
                InlineKeyboardButton.CallbackData(
                    text = localizationManager.getString(StringKey.MENU_ACTION_TRADE),
                    callbackData = ACTION_START_TRADING
                ),
                InlineKeyboardButton.CallbackData(
                    text = localizationManager.getString(StringKey.MENU_ACTION_STATS),
                    callbackData = ACTION_STATS
                ),
                InlineKeyboardButton.CallbackData(
                    text = localizationManager.getString(StringKey.MENU_ACTION_EXIT),
                    callbackData = ACTION_EXIT
                )
            )
        )

        bot.sendMessage(
            chatId = chatId,
            text = localizationManager.getString(StringKey.MENU_TITLE),
            replyMarkup = inlineKeyboard
        )
    }

    fun handleUserCallback(data: String) {
        if (languageHandler.handleLanguageSelection(data)) {
            localizationManager = languageHandler.selectedLanguage!!
            onStart()
            return
        }

        when (data) {
            ACTION_SHOW_BALANCE -> {
                currentAction = UserAction.Balance
                if (account == null) {
                    startInputHandler(AccountInputHandler(bot, chatId, localizationManager))
                } else {
                    onAccountReceived(account!!)
                }
            }

            ACTION_START_TRADING -> {
                if (currentAction == UserAction.Volume) {
                    sendMessage(localizationManager.getString(StringKey.TRADE_IS_ACTIVE_WARNING))
                    return
                }
                currentAction = UserAction.Volume
                if (account == null) {
                    startInputHandler(AccountInputHandler(bot, chatId, localizationManager))
                } else {
                    onAccountReceived(account!!)
                }
            }

            ACTION_STATS -> {
                currentAction = UserAction.Stats
                if (account == null) {
                    startInputHandler(AccountInputHandler(bot, chatId, localizationManager))
                } else {
                    onAccountReceived(account!!)
                }
            }

            ACTION_EXIT -> {
                exit()
            }
        }
    }

    fun requestStop() {
        resetUserData()
        sendMessage(localizationManager.getString(StringKey.EXECUTE_AGAIN_MESSAGE))
        onFinish.invoke(chat)
    }

    private fun onAccountReceived(account: CredentialAccount) {
        this.account = account
        finishUserInput()
        when (currentAction) {
            UserAction.Balance -> showBalances(account)
            UserAction.Volume -> startInputHandler(TradeConfigInputHandler(bot, chatId, localizationManager))
            UserAction.Stats -> showStatistic(account)
            else -> restart()
        }
    }

    private fun onTradeConfigReceived(config: TradeConfig) {
        finishUserInput()
        if (currentAction == UserAction.Volume) {
            val currentAccount = account ?: run {
                restart()
                return
            }
            startTrade(currentAccount, config)
        } else {
            restart()
        }
    }

    private fun showBalances(account: CredentialAccount) {
        buildClient(account) { client ->
            val showBalanceCase = ShowBalanceCase(client, logger).also { runningFlow = it }
            scope.launch {
                showBalanceCase()
                runningFlow = null
                showActionMenu()
            }
        }
    }

    private fun startTrade(account: CredentialAccount, tradeConfig: TradeConfig) {
        sendMessage(localizationManager.getString(StringKey.TRADE_STOP_HINT))
        buildClient(account) { client ->
            val startVolumeTradeCase = StartVolumeTradeCase(client, logger).also { runningFlow = it }
            scope.launch {
                startVolumeTradeCase(tradeConfig)
                runningFlow = null
                showActionMenu()
            }
        }
    }

    private fun showStatistic(account: CredentialAccount) {
        buildClient(account) { client ->
            val showStatsCase = ShowStatsCase(client, logger).also { runningFlow = it }
            scope.launch {
                showStatsCase()
                runningFlow = null
                showActionMenu()
            }
        }
    }

    private fun onError(ex: Throwable) {
        sendMessage("${localizationManager.getString(StringKey.ERROR_MESSAGE)} ${ex.message}")
        restart()
    }

    private fun restart() {
        sendMessage(localizationManager.getString(StringKey.INPUT_ERROR_MESSAGE))
        onStart()
    }

    private fun exit() {
        resetUserData()
        sendMessage(localizationManager.getString(StringKey.FINAL_MESSAGE))
        onFinish(chat)
    }

    private inline fun buildClient(account: CredentialAccount, action: (ArkmClient) -> Unit) {
        val httpClient = createOkHttpClient(account)
        action(ArkmClient(httpClient, logger))
    }

    private enum class UserAction {
        Balance,
        Volume,
        Stats,
        Exit
    }

    private companion object {
        const val ACTION_SHOW_BALANCE = "show_balances"
        const val ACTION_START_TRADING = "start_trading"
        const val ACTION_STATS = "stats"
        const val ACTION_EXIT = "exit"
    }
}

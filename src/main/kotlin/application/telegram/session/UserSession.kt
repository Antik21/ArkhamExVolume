package application.telegram.session

import application.telegram.TelegramLogger
import application.telegram.text_handler.AccountInputHandler
import application.telegram.text_handler.InputTextHandler
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
import use_case.ShowStatsCase

class UserSession(private val bot: Bot, private val chat: Chat, private val onFinish: () -> Unit) {

    private val chatId = ChatId.fromId(chat.id)
    private val logger = TelegramLogger(bot, chatId)
    private var currentAction: UserAction? = null
    private var inputHandler: InputTextHandler<*>? = null
    private var account: CredentialAccount? = null

    init {
        onStart()
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
            sendMessage("Привет ${chat.firstName}")
        }
        resetUserData()
        showActionMenu()
    }

    private fun resetUserData() {
        currentAction = null
        inputHandler = null
        account = null
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
        } else {
            bot.sendMessage(chatId = chatId, text = "Неизвестная команда. Начните с /start.")
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
                InlineKeyboardButton.CallbackData(text = "Баланс", callbackData = ACTION_SHOW_BALANCE),
                InlineKeyboardButton.CallbackData(text = "Торговля", callbackData = ACTION_START_TRADING),
                InlineKeyboardButton.CallbackData(text = "Статистика", callbackData = ACTION_STATS),
                InlineKeyboardButton.CallbackData(text = "Выход", callbackData = ACTION_EXIT)
            )
        )

        bot.sendMessage(
            chatId = chatId,
            text = "Выберите операцию:",
            replyMarkup = inlineKeyboard
        )
    }

    fun handleUserCallback(data: String) {
        when (data) {
            ACTION_SHOW_BALANCE -> {
                currentAction = UserAction.Balance
                if (account == null) {
                    startInputHandler(AccountInputHandler(bot, chatId))
                } else {
                    onAccountReceived(account!!)
                }
            }

            ACTION_START_TRADING -> {
                currentAction = UserAction.Volume
                if (account == null) {
                    startInputHandler(AccountInputHandler(bot, chatId))
                } else {
                    onAccountReceived(account!!)
                }
            }

            ACTION_STATS -> {
                currentAction = UserAction.Stats
                if (account == null) {
                    startInputHandler(AccountInputHandler(bot, chatId))
                } else {
                    onAccountReceived(account!!)
                }
            }

            ACTION_EXIT -> {
                exit()
            }
        }
    }

    fun forceStop(){

    }

    private fun onAccountReceived(account: CredentialAccount) {
        this.account = account
        finishUserInput()
        when (currentAction) {
            UserAction.Balance -> {
                showBalances(account)
            }

            UserAction.Volume -> {
                startInputHandler(TradeConfigInputHandler(bot, chatId))
            }

            UserAction.Stats -> {
                showStatistic(account)
            }

            else -> {
                restart()
            }
        }
    }

    private fun onTradeConfigReceived(config: TradeConfig) {
        finishUserInput()
        when (currentAction) {
            UserAction.Volume -> {
                val currentAccount = account
                if (currentAccount == null) {
                    restart()
                    return
                }
                startTrade(currentAccount, config)
            }

            else -> {
                restart()
            }
        }
    }

    private fun showBalances(account: CredentialAccount) {
        buildClient(account) { client ->
            val showBalanceCase = ShowBalanceCase(client, logger)

            runCatching {
                showBalanceCase()
                showActionMenu()
            }.onFailure {
                sendMessage("Ошибка при получении балансов: ${it.message})")
                restart()
            }
        }
    }

    private fun startTrade(account: CredentialAccount, tradeConfig: TradeConfig) {
        buildClient(account) { client ->
            val startVolumeTradeCase = StartVolumeTradeCase(client, logger)

            runCatching {
                startVolumeTradeCase(tradeConfig)
                showActionMenu()
            }.onFailure {
                sendMessage("Ошибка при в процессе торговли: ${it.message})")
                restart()
            }
        }
    }

    private fun showStatistic(account: CredentialAccount) {
        buildClient(account) { client ->
            val showStatsCase = ShowStatsCase(client, logger)

            runCatching {
                showStatsCase()
                showActionMenu()
            }.onFailure {
                sendMessage("Ошибка при получении статистики: ${it.message})")
                restart()
            }
        }
    }

    private fun restart() {
        sendMessage(buildString {
            appendLine("Возникли проблемы, проверьте введенные данные!")
            appendLine("Давай попробуем еще раз.")
        })
        onStart()
    }

    private fun exit() {
        resetUserData()
        sendMessage("Спасибо за использование! До свидания!")
        onFinish.invoke()
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
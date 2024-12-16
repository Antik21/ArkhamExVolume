package application.telegram.text_handler

import com.antik.utils.user.CredentialAccount
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import generated.StringKey
import localization.LocalizationManager
import user.parseAccount

class AccountInputHandler(private val bot: Bot, private val chatId: ChatId, private val localizationManager: LocalizationManager) : InputTextHandler<CredentialAccount> {
    private var account: CredentialAccount? = null

    override fun getStartMessage(): String {
        return localizationManager.getString(StringKey.ACCOUNT_START_MESSAGE)
    }

    override fun getCompleteMessage(): String {
        return localizationManager.getString(StringKey.ACCOUNT_FINAL_MESSAGE)
    }

    override fun handleInput(input: String) {
        parseAccount(input)
            .onSuccess {
                account = it
            }
            .onFailure {
                bot.sendMessage(
                    chatId = chatId,
                    text = localizationManager.getString(StringKey.ACCOUNT_WRONG_DATA)
                )
            }
    }

    override fun nextStepMessage(): String? {
        return if (account == null) getStartMessage() else null
    }

    override fun isComplete(): Boolean = account != null

    override fun getResult(): CredentialAccount? = account
}

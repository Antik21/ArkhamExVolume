package application.telegram.text_handler

import com.antik.utils.user.CredentialAccount
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import user.parseAccount

class AccountInputHandler(private val bot: Bot, private val chatId: ChatId) : InputTextHandler<CredentialAccount> {
    private var account: CredentialAccount? = null

    override fun getStartMessage(): String {
        return "Введите данные аккаунта в формате: `accountId:apiKey:apiSecretKey (+опционально):proxyIp:proxyPort:proxyLogin:proxyPassword`"
    }

    override fun getCompleteMessage(): String {
        return "Данные аккаунта успешно сохранены!"
    }

    override fun handleInput(input: String) {
        parseAccount(input)
            .onSuccess {
                account = it
            }
            .onFailure {
                bot.sendMessage(
                    chatId = chatId,
                    text = "Неверный формат данных аккаунта. Попробуйте снова."
                )
            }
    }

    override fun nextStepMessage(): String? {
        return if (account == null) getStartMessage() else null
    }

    override fun isComplete(): Boolean = account != null

    override fun getResult(): CredentialAccount? = account
}

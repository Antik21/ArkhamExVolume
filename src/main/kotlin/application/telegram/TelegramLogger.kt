package application.telegram

import com.antik.utils.logger.Logger
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TelegramLogger(
    private val bot: Bot,
    private val chatId: ChatId
) : Logger {

    private companion object {
        const val LOG_TEMPLATE = "[%s:%s] %s"
    }

    override fun debug(tag: String, msg: String) {
        logToFile(tag,msg)
    }

    override fun message(msg: String) {
        bot.sendMessage(
            chatId = chatId,
            text = msg
        )
    }

    private fun logToFile(tag: String, msg: String) {
        val currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val logEntry = LOG_TEMPLATE.format(currentDateTime, tag, msg) + "\n"
        val file = File("logs.txt")
        file.appendText(logEntry)
    }
}
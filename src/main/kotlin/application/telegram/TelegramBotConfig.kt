package application.telegram

import java.io.File
import java.util.Properties

object TelegramBotConfig {
    val ACCESS_TOKEN: String by lazy {
        System.getenv("TELEGRAM_ACCESS_TOKEN")
            ?: loadFromLocalFile()
            ?: throw IllegalStateException(
                "TELEGRAM_ACCESS_TOKEN is not set in the environment variables or local.properties file"
            )
    }

    private fun loadFromLocalFile(): String? {
        val configFile = File("local.properties")
        if (configFile.exists()) {
            val properties = Properties().apply {
                load(configFile.inputStream())
            }
            return properties.getProperty("TELEGRAM_ACCESS_TOKEN")
        }
        return null
    }
}

package application.telegram

object TelegramBotConfig {
    val ACCESS_TOKEN: String = System.getenv("TELEGRAM_ACCESS_TOKEN")
        ?: throw IllegalStateException("TELEGRAM_ACCESS_TOKEN is not set in the environment variables")
}
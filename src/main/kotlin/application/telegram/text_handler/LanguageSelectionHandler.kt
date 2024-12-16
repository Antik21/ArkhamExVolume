package application.telegram.text_handler

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import localization.Language
import localization.LocalizationManager

class LanguageSelectionHandler(private val bot: Bot, private val chatId: ChatId) {

    var selectedLanguage: LocalizationManager? = null
        private set

    fun showLanguageMenu() {
        val inlineKeyboard = InlineKeyboardMarkup.create(
            listOf(
                InlineKeyboardButton.CallbackData("Русский", CALLBACK_LANGUAGE_RU),
                InlineKeyboardButton.CallbackData("English", CALLBACK_LANGUAGE_EN)
            )
        )

        bot.sendMessage(
            chatId = chatId,
            text = "Выберите язык / Select a language:",
            replyMarkup = inlineKeyboard
        )
    }

    fun handleLanguageSelection(callbackData: String): Boolean {
        selectedLanguage = when (callbackData) {
            CALLBACK_LANGUAGE_RU -> LocalizationManager.getInstance(Language.Russian)
            CALLBACK_LANGUAGE_EN -> LocalizationManager.getInstance(Language.English)
            else -> null
        }

        return selectedLanguage != null
    }

    companion object {
        private const val CALLBACK_LANGUAGE_RU = "language_ru"
        private const val CALLBACK_LANGUAGE_EN = "language_en"
    }
}

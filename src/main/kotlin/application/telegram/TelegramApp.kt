package application.telegram

import application.console.logUserToFile
import application.telegram.TelegramBotConfig.ACCESS_TOKEN
import application.telegram.session.UserSession
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.CallbackQuery
import com.github.kotlintelegrambot.entities.Chat

private val sessions = mutableMapOf<Long, UserSession>()

fun main() {
    val bot = bot {
        token = ACCESS_TOKEN

        dispatch {
            command("start") {
                startSession(bot, message.chat)
            }

            command("stop") {
                stopSession(message.chat)
            }

            text {
                if (!text.startsWith("/")) {
                    handleText(bot, message.chat, text)
                }
            }

            callbackQuery {
                handleCallback(bot, callbackQuery)
            }
        }
    }

    bot.startPolling()
}

private fun startSession(bot: Bot, chat: Chat) {
    sessions[chat.id] = UserSession(bot, chat) {
        sessions.remove(chat.id)
    }
    logUserToFile(chat)
}

private fun stopSession(chat: Chat) {
    sessions[chat.id]?.stop()
}

private fun handleText(bot: Bot, chat: Chat, text: String) {
    sessions[chat.id]?.handleUserInput(text) ?: startSession(bot, chat)
}

private fun handleCallback(bot: Bot, callback: CallbackQuery) {
    val chat = callback.message?.chat ?: return
    sessions[chat.id]?.handleUserCallback(callback.data) ?: startSession(bot, chat)
}

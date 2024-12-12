package application.console

import com.github.kotlintelegrambot.entities.Chat
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun logUserToFile(chat: Chat) {
    val currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

    val userId = chat.id
    val firstName = chat.firstName ?: "Unknown"
    val lastName = chat.lastName ?: "Unknown"
    val username = chat.username ?: "Unknown"
    val profile = "https://t.me/$username"

    val logEntry = "$currentDateTime, UserID: $userId, Name: $firstName $lastName, Profile: $profile\n"

    val file = File("users.txt")
    file.appendText(logEntry)
}

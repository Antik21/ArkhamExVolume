package com.antik.utils.user
import user.parseAccount
import java.io.File

object UserRepository {

    fun loadCredentialsFromFile(filePath: String): List<CredentialAccount> {
        val credentials = mutableListOf<CredentialAccount>()

        try {
            File(filePath).forEachLine { line ->
                val account = parseAccount(line).getOrThrow()
                credentials.add(account)
            }
        } catch (e: Exception) {
            println("Error reading credentials file: ${e.message}")
        }

        return credentials
    }
}
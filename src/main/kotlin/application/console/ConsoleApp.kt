package application.console

import arkham.token.Token
import com.antik.utils.arkham.ArkmClient
import com.antik.utils.arkham.createOkHttpClient
import com.antik.utils.logger.Logger
import com.antik.utils.use_case.ShowBalanceCase
import com.antik.utils.use_case.StartVolumeTradeCase
import com.antik.utils.use_case.TradeConfig
import com.antik.utils.user.CredentialAccount
import com.antik.utils.user.UserRepository.loadCredentialsFromFile
import use_case.ShowStatsCase
import java.util.*
import kotlin.system.exitProcess

fun main() {
    val scanner = Scanner(System.`in`)
    while (true) {
        println(
            """
            === Menu ===
            1 - Show balances
            2 - Start trade
            3 - Stats
            4 - Exit
            Enter your choice:
            """.trimIndent()
        )

        when (scanner.nextLine()) {
            "1" -> showBalances()
            "2" -> startTrade(scanner)
            "3" -> showStats()
            "4" -> {
                println("Exiting the program.")
                break
            }

            else -> println("Invalid choice. Please try again.")
        }

        println("\n==========================\n")
    }
}

private fun showBalances() {
    accounts.forEach { account ->
        buildClient(account) { client, logger ->
            val showBalanceCase = ShowBalanceCase(client, logger)
            runCatching {
                showBalanceCase.invoke()
            }.onFailure {
                logger.message("Error fetching balances: ${it.message}")
            }
        }
    }
}

private fun startTrade(scanner: Scanner) {
    val config = createTradeConfig(scanner)

    accounts.forEach { account ->
        buildClient(account) { client, logger ->
            val startVolumeTradeCase = StartVolumeTradeCase(client, logger)
            runCatching {
                startVolumeTradeCase.invoke(config)
            }.onFailure {
                logger.message("Error during trading: ${it.message}")
            }
        }
    }
}

private fun showStats() {
    accounts.forEach { account ->
        buildClient(account) { client, logger ->
            val showStatsCase = ShowStatsCase(client, logger)
            runCatching {
                showStatsCase.invoke()
            }.onFailure {
                logger.message("Error fetching statistic: ${it.message}")
            }
        }
    }
}

private inline fun buildClient(account: CredentialAccount, block: (ArkmClient, Logger) -> Unit) {
    val logger = account.logger()
    val httpClient = createOkHttpClient(account)
    val client = ArkmClient(httpClient, logger)
    block(client, logger)
}

private fun createTradeConfig(scanner: Scanner): TradeConfig {
    fun prompt(message: String, defaultValue: String): String {
        println("$message (default: $defaultValue):")
        return scanner.nextLine().ifBlank { defaultValue }
    }

    println("Supported tokens: ${Token.entries.joinToString(", ") { it.symbol }}")

    val inputTokens = prompt("Enter trade coins (separate by comma)", "BTC")
    val tokens = inputTokens.split(",")
        .map { it.trim().uppercase() }
        .mapNotNull { symbol ->
            try {
                Token.fromSymbol(symbol)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
        .toSet()

    val selectedTokens = tokens.ifEmpty {
        println("No valid tokens provided. Defaulting to BTC.")
        setOf(Token.Bitcoin)
    }

    val waitBeforeSell = prompt("Enter wait time before selling in seconds", "10").toIntOrNull() ?: 10
    val waitBetweenCycles = prompt("Enter wait time between buy/sell cycles in seconds", "30").toIntOrNull() ?: 30
    val timeRange = prompt("Enter time range for random intervals in seconds", "5").toIntOrNull() ?: 5
    val maxVolume = prompt("Enter max volume in USD", "1000.0").toDoubleOrNull() ?: 1000.0
    val minBalanceUSD = prompt("Enter minimum balance in USD to continue trading", "50.0").toDoubleOrNull() ?: 50.0

    return TradeConfig(
        tokens = selectedTokens,
        waitBeforeSell = waitBeforeSell,
        waitBetweenCycles = waitBetweenCycles,
        timeRange = timeRange,
        maxVolume = maxVolume,
        minBalanceUSD = minBalanceUSD
    )
}

private val accounts: List<CredentialAccount> by lazy {
    loadAccounts()
}

private fun CredentialAccount.logger(): ConsoleLogger {
    return ConsoleLogger(accountId)
}

private fun loadAccounts(): List<CredentialAccount> {
    val filePath = "accounts.txt"
    val accounts = loadCredentialsFromFile(filePath)
    if (accounts.isEmpty()) {
        println("No accounts found in $filePath. Exiting.")
        exitProcess(0)
    }
    return accounts
}

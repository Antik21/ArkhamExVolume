package com.antik.utils.use_case

import com.antik.utils.arkham.ArkmClient
import com.antik.utils.logger.Logger
import kotlinx.coroutines.runBlocking

class ShowBalanceCase(private val client: ArkmClient, private val logger: Logger) {

    operator fun invoke() = runBlocking {
        val balances = client.getBalances()

        if (balances.isNullOrEmpty()) {
            logger.message("No balances found.")
            return@runBlocking
        }

        balances.forEach { balance ->
            val amount = balance.balance;
            if (amount != "0") {
                logger.message("${balance.symbol}: $amount")
            }
        }

        val usdSum = balances.sumOf { it.balanceUSDT.toDouble() }
        logger.message("Sum: $usdSum $")
    }
}
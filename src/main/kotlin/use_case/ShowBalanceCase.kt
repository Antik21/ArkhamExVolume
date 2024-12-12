package com.antik.utils.use_case

import com.antik.utils.arkham.ArkmClient
import com.antik.utils.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import use_case.FlowCase

class ShowBalanceCase(private val client: ArkmClient, private val logger: Logger) : FlowCase {

    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        val balances = client.getBalances()

        if (balances.isNullOrEmpty()) {
            logger.message("No balances found.")
            return@withContext
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

    override fun forceStop() { }
}
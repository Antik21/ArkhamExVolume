package com.antik.utils.use_case

import arkham.token.Token
import com.antik.utils.arkham.ArkmClient
import com.antik.utils.arkham.request.OrderRequest
import com.antik.utils.arkham.request.OrderSide
import com.antik.utils.arkham.request.OrderType
import com.antik.utils.arkham.response.Balance
import com.antik.utils.arkham.response.OrderResponse
import com.antik.utils.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import use_case.FlowCase
import java.util.*

class StartVolumeTradeCase(
    private val client: ArkmClient,
    private val logger: Logger,
) : FlowCase {

    private companion object {
        const val MIN_TRADE_AMOUNT = 10.0
    }

    private var totalVolume = 0.0
    private var pendingStop: Boolean = false

    override fun forceStop() {
        pendingStop = true
        logger.message("Торговля остановится в начале или в конце оборота")
    }

    suspend operator fun invoke(config: TradeConfig) = withContext(Dispatchers.IO) {
        logger.message("Starting trade for tokens: ${config.tokens.joinToString(", ") { it.symbol }}")

        while (true) {
            if (validateStopRequest()) return@withContext

            val tradeToken = config.tokens.random()
            val tradingPair = getTradingPair(tradeToken, config.leverage)
            logger.message("Starting trade for pair: $tradingPair")

            val balances = fetchBalances()
            val usdBalance = balances.sumOf { it.balanceUSDT.toDouble() }
            validateBalance(usdBalance, config.minBalanceUSD)

            val usdtBalance = balances.find { it.symbol == "USDT" }?.free?.toDoubleOrNull()
            validateUSDTBalance(usdtBalance)

            val remainingTradeVolume = (config.maxVolume - totalVolume) / 1.93
            val buyAmount = calculateBuyAmount(usdtBalance, remainingTradeVolume, config.leverage)

            val buySize = getTradeSize(tradeToken, tradingPair, buyAmount)
            val buyResponse = executeOrder(
                tradingPair = tradingPair,
                side = OrderSide.BUY,
                size = buySize,
                maxAttempts = 3,
                delayMillis = 500,
            )
            val boughtSize = buyResponse.size.toDouble()
            totalVolume += buyResponse.getOrderVolume()

            delay(calculateRandomDelay(config.waitBeforeSell, config.timeRange) * 1000L)

            val tokenBalance = if(config.leverage == 1) boughtSize.let { size ->
                val roundingStep = tradeToken.roundingStep
                val roundedBalance = size - (size % roundingStep)
                roundedBalance.coerceAtLeast(0.0)
            } else boughtSize

            validateTokenBalance(tokenBalance, tradeToken.symbol)

            val sellResponse = executeOrder(
                tradingPair = tradingPair,
                side = OrderSide.SELL,
                size = tokenBalance,
                maxAttempts = 3,
                delayMillis = 1000,
            )
            totalVolume += sellResponse.getOrderVolume()

            if (totalVolume >= config.maxVolume) {
                logger.message("Trade completed successfully. Total volume: $totalVolume USD.")
                return@withContext
            } else {
                logger.message("Current total volume: $totalVolume USD.")
            }

            if (validateStopRequest()) return@withContext

            delay(calculateRandomDelay(config.waitBetweenCycles, config.timeRange) * 1000L)
        }
    }

    private fun getTradingPair(token: Token, leverage: Int): String {
        return if (leverage > 1) "${token.symbol}_USDT_PERP" else "${token.symbol}_USDT"
    }

    private fun validateStopRequest(): Boolean {
        return if (pendingStop) {
            logger.message("Trade was stopped. Total volume: $totalVolume USD.")
            true
        } else false
    }

    private suspend fun fetchBalances(): List<Balance> {
        return retryOperation(maxAttempts = 3, delayMillis = 1000) {
            client.getBalances()
        } ?: throw Exception("Failed to fetch balances.")
    }

    private fun validateBalance(usdBalance: Double, minBalanceUSD: Double) {
        if (usdBalance < minBalanceUSD) {
            throw Exception("Stopping trade. Balance ($usdBalance USD) is below minimum ($minBalanceUSD USD).")
        }
    }

    private fun validateUSDTBalance(usdtBalance: Double?) {
        if (usdtBalance == null || usdtBalance < 1) {
            throw Exception("Insufficient USDT balance.")
        }
    }

    private fun calculateBuyAmount(usdtBalance: Double?, remainingTradeVolume: Double, leverage: Int): Double {
        val leveragedBalance = (usdtBalance ?: 0.0) * leverage
        return minOf(remainingTradeVolume, leveragedBalance).takeIf {
            it >= MIN_TRADE_AMOUNT
        } ?: MIN_TRADE_AMOUNT
    }

    private fun validateTokenBalance(tokenBalance: Double?, tokenSymbol: String) {
        if (tokenBalance == null || tokenBalance <= 0) {
            throw Exception("Failed to fetch balance for $tokenSymbol.")
        }
    }

    private suspend fun executeOrder(
        tradingPair: String,
        side: OrderSide,
        size: Double,
        maxAttempts: Int,
        delayMillis: Long,
    ): OrderResponse {
        return retryOperation(maxAttempts = maxAttempts, delayMillis = delayMillis) {
            client.createOrder(
                OrderRequest(
                    clientOrderId = UUID.randomUUID().toString(),
                    postOnly = false,
                    side = side,
                    size = size.toSize(),
                    symbol = tradingPair,
                    type = OrderType.MARKET
                )
            )
        } ?: throw Exception("Failed to create `$side` order after $maxAttempts attempts.")
    }

    private fun OrderResponse.getOrderVolume(): Double {
        return size.toDouble() * price.toDouble()
    }

    private suspend fun getTradeSize(
        token: Token,
        pair: String,
        size: Double,
    ): Double {
        val ticker = retryOperation(3, 500) {
            client.getTicker(pair)
        } ?: throw Exception("Failed to fetch ticker for $pair.")

        val currentPrice = ticker.price.toDoubleOrNull()
        if (currentPrice == null || currentPrice <= 0) {
            throw Exception("Invalid price for $pair: ${ticker.price}")
        }

        val tradeSize = calculateTradeSize(size, currentPrice, token.roundingStep)
        return tradeSize.coerceAtLeast(0.0)
    }

    private fun calculateTradeSize(size: Double, currentPrice: Double, roundingStep: Double): Double {
        val tradeSize = (size * 0.99) / currentPrice
        return if (roundingStep == 1.0) {
            tradeSize.toInt().toDouble()
        } else {
            tradeSize - (tradeSize % roundingStep)
        }
    }

    private fun calculateRandomDelay(baseDelay: Int, range: Int): Int {
        val min = (baseDelay - range).coerceAtLeast(0)
        val max = baseDelay + range
        return (min..max).random()
    }

    private suspend fun <T> retryOperation(
        maxAttempts: Int,
        delayMillis: Long,
        operation: suspend () -> T?,
    ): T? {
        repeat(maxAttempts) { attempt ->
            val result = operation()
            if (result != null) {
                return result
            }
            logger.debug("RetryOperation", "Attempt ${attempt + 1} failed. Retrying in ${delayMillis / 1000} seconds.")
            delay(delayMillis)
        }
        return null
    }

    private fun Double.toSize(): String {
        return "%.10f".format(this).trimEnd('0').trimEnd('.')
    }
}


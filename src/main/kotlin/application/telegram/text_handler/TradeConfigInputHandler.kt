package application.telegram.text_handler

import arkham.token.Token
import com.antik.utils.use_case.TradeConfig
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import generated.StringKey
import localization.LocalizationManager

class TradeConfigInputHandler(private val bot: Bot, private val chatId: ChatId, private val localizationManager: LocalizationManager) : InputTextHandler<TradeConfig> {

    private enum class TradeConfigStep {
        TOKENS,
        LEVERAGE,
        WAIT_BEFORE_SELL,
        WAIT_BETWEEN_CYCLES,
        TIME_RANGE,
        MAX_VOLUME,
        MIN_BALANCE_USD,
        COMPLETE
    }

    private var step: TradeConfigStep = TradeConfigStep.TOKENS
    private val configData = mutableMapOf<String, Any>()

    override fun getStartMessage(): String {
        return buildString {
            appendLine(localizationManager.getString(StringKey.TRADE_CONFIG_TITLE))
            appendLine(localizationManager.getString(StringKey.TRADE_SUPPORTED_TOKENS, Token.entries.joinToString(", ") { it.symbol }))
            append(step.message())
        }
    }

    override fun getCompleteMessage(): String {
        return localizationManager.getString(StringKey.TRADE_CONFIG_CREATED)
    }

    override fun handleInput(input: String) {
        when (step) {
            TradeConfigStep.TOKENS -> {
                val tokens = input.split(",")
                    .map { it.trim().uppercase() }
                    .mapNotNull { symbol ->
                        try {
                            Token.fromSymbol(symbol)
                        } catch (e: IllegalArgumentException) {
                            null
                        }
                    }
                    .toSet()

                configData["tokens"] = tokens.ifEmpty {
                    bot.sendMessage(
                        chatId = chatId,
                        text = localizationManager.getString(StringKey.TRADE_CONFIG_INVALID_TOKENS)
                    )
                    setOf(Token.Bitcoin)
                }
                step = TradeConfigStep.LEVERAGE
            }

            TradeConfigStep.LEVERAGE -> {
                configData["leverage"] = input.toIntOrNull()?.coerceIn(1, 10) ?: 1
                step = TradeConfigStep.WAIT_BEFORE_SELL
            }

            TradeConfigStep.WAIT_BEFORE_SELL -> {
                configData["waitBeforeSell"] = input.toIntOrNull() ?: 10
                step = TradeConfigStep.WAIT_BETWEEN_CYCLES
            }

            TradeConfigStep.WAIT_BETWEEN_CYCLES -> {
                configData["waitBetweenCycles"] = input.toIntOrNull() ?: 30
                step = TradeConfigStep.TIME_RANGE
            }

            TradeConfigStep.TIME_RANGE -> {
                configData["timeRange"] = input.toIntOrNull()?.takeIf { it >= 0 } ?: 5
                step = TradeConfigStep.MAX_VOLUME
            }

            TradeConfigStep.MAX_VOLUME -> {
                configData["maxVolume"] = input.toDoubleOrNull() ?: 1000.0
                step = TradeConfigStep.MIN_BALANCE_USD
            }

            TradeConfigStep.MIN_BALANCE_USD -> {
                configData["minBalanceUSD"] = input.toDoubleOrNull() ?: 50.0
                step = TradeConfigStep.COMPLETE
            }

            TradeConfigStep.COMPLETE -> {
                bot.sendMessage(chatId = chatId, text = localizationManager.getString(StringKey.TRADE_CONFIG_COMPLETE))
            }
        }
    }

    override fun nextStepMessage(): String {
        return step.message()
    }

    override fun isComplete(): Boolean = step == TradeConfigStep.COMPLETE

    override fun getResult(): TradeConfig? {
        return if (isComplete()) {
            TradeConfig(
                tokens = configData["tokens"] as Set<Token>,
                leverage = configData["leverage"] as Int,
                waitBeforeSell = configData["waitBeforeSell"] as Int,
                waitBetweenCycles = configData["waitBetweenCycles"] as Int,
                timeRange = configData["timeRange"] as Int,
                maxVolume = configData["maxVolume"] as Double,
                minBalanceUSD = configData["minBalanceUSD"] as Double
            )
        } else null
    }

    private fun TradeConfigStep.message(): String {
        return when (this) {
            TradeConfigStep.TOKENS -> localizationManager.getString(StringKey.TRADE_CONFIG_TOKENS)
            TradeConfigStep.LEVERAGE -> localizationManager.getString(StringKey.TRADE_CONFIG_LEVERAGE)
            TradeConfigStep.WAIT_BEFORE_SELL -> localizationManager.getString(StringKey.TRADE_CONFIG_WAIT_BEFORE_SELL)
            TradeConfigStep.WAIT_BETWEEN_CYCLES -> localizationManager.getString(StringKey.TRADE_CONFIG_WAIT_BETWEEN_CYCLES)
            TradeConfigStep.TIME_RANGE -> localizationManager.getString(StringKey.TRADE_CONFIG_TIME_RANGE)
            TradeConfigStep.MAX_VOLUME -> localizationManager.getString(StringKey.TRADE_CONFIG_MAX_VOLUME)
            TradeConfigStep.MIN_BALANCE_USD -> localizationManager.getString(StringKey.TRADE_CONFIG_MIN_BALANCE_USD)
            TradeConfigStep.COMPLETE -> localizationManager.getString(StringKey.TRADE_CONFIG_COMPLETE)
        }
    }
}

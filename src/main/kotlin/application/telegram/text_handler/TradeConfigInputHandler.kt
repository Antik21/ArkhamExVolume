package application.telegram.text_handler

import arkham.token.Token
import com.antik.utils.use_case.TradeConfig
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId

class TradeConfigInputHandler(private val bot: Bot, private val chatId: ChatId) : InputTextHandler<TradeConfig> {

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
            appendLine("Укажите конфигурацию для трейдинга.")
            appendLine("Поддерживаемые монеты: ${Token.entries.joinToString(", ") { it.symbol }}")
            append(TradeConfigStep.TOKENS.message())
        }
    }

    override fun getCompleteMessage(): String {
        return "Конфигурация успешно создана!"
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
                    bot.sendMessage(chatId = chatId, text = "Некорректные данные. Используется BTC по умолчанию.")
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
                bot.sendMessage(chatId = chatId, text = "Все данные уже заполнены.")
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
            TradeConfigStep.TOKENS -> "Введите список монет для торговли через запятую (например: BTC, ETH):"
            TradeConfigStep.LEVERAGE -> "Введите торговое плечо (1 для Спота, 2-10 для Perp торговли, по умолчанию: 1)"
            TradeConfigStep.WAIT_BEFORE_SELL -> "Введите время ожидания перед продажей (в секундах, по умолчанию: 10):"
            TradeConfigStep.WAIT_BETWEEN_CYCLES -> "Введите время ожидания между циклами (в секундах, по умолчанию: 30):"
            TradeConfigStep.TIME_RANGE -> "Введите диапазон в секундах для случайных интервалов (по умолчанию: 5):"
            TradeConfigStep.MAX_VOLUME -> "Введите максимальный объем (в USD, по умолчанию: 1000):"
            TradeConfigStep.MIN_BALANCE_USD -> "Введите минимальный баланс (в USD, по умолчанию: 50):"
            TradeConfigStep.COMPLETE -> "Все данные уже заполнены."
        }
    }
}

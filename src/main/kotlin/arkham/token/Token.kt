package arkham.token

enum class Token(val symbol: String, val roundingStep: Double) {
    Bitcoin("BTC", 0.00001),
    Ethereum("ETH", 0.0001),
    Solana("SOL", 0.001),
    Arkham("ARKM", 0.1),
    Doge("DOGE", 1.0),
    PEPE("PEPE", 1.0),
    Dogwifhat("WIF", 0.01),
    Toncoin("TON", 0.01),
    Avalanche("AVAX", 0.01);

    companion object {
        fun fromSymbol(symbol: String): Token? {
            return entries.firstOrNull {
                it.symbol == symbol
            }
        }
    }
}
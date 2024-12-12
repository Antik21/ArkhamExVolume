package arkham.response

data class TickerResponse(
    val baseSymbol: String,
    val fundingRate: String,
    val high24h: String,
    val indexCurrency: String,
    val indexPrice: String,
    val low24h: String,
    val markPrice: String,
    val nextFundingRate: String,
    val nextFundingTime: Long,
    val openInterest: String,
    val openInterestUSD: String,
    val price: String,
    val price24hAgo: String,
    val productType: String, // "spot" or "perpetual"
    val quoteSymbol: String,
    val quoteVolume24h: String,
    val symbol: String,
    val usdVolume24h: String,
    val volume24h: String
)
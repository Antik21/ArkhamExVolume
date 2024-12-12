package arkham.response

data class TradingVolumeStatsResponse(
    val perpMakerFees: String,
    val perpMakerVolume: String,
    val perpTakerFees: String,
    val perpTakerVolume: String,
    val perpVolume: List<VolumeEntry>,
    val spotMakerFees: String,
    val spotMakerVolume: String,
    val spotTakerFees: String,
    val spotTakerVolume: String,
    val spotVolume: List<VolumeEntry>,
    val totalVolume: List<VolumeEntry>,
)

data class VolumeEntry(
    val size: String,
    val time: Long,
)

data class PointsResponse(
    val points: Long,
    val rank: Long,
)
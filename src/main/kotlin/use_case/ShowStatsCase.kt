package use_case

import com.antik.utils.arkham.ArkmClient
import com.antik.utils.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ShowStatsCase(private val client: ArkmClient, private val logger: Logger) : FlowCase {

    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        val stats = client.getTradingVolumeStats() ?: throw Exception("Failed to fetch volume statistic.")
        val points = client.getPoints() ?: throw Exception("Failed to fetch rank.")

        val statsMessage = buildString {
            appendLine("Points: ${points.points}")
            appendLine("Rank: ${points.rank}")
            appendLine("Spot volume: ${stats.spotVolume.sumOf { it.size.toDoubleOrNull() ?: 0.0 }}")
            appendLine("Perp volume: ${stats.perpVolume.sumOf { it.size.toDoubleOrNull() ?: 0.0 }}")
        }
        logger.message(statsMessage)
    }

    override fun forceStop() {}
}
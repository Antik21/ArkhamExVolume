package use_case

import com.antik.utils.arkham.ArkmClient
import com.antik.utils.logger.Logger
import kotlinx.coroutines.runBlocking

class ShowStatsCase(private val client: ArkmClient, private val logger: Logger) {

    operator fun invoke() = runBlocking {
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
}
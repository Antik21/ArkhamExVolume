package arkham.service

import arkham.response.PointsResponse
import arkham.response.TradingVolumeStatsResponse
import com.skydoves.sandwich.ApiResponse
import retrofit2.http.GET

interface StatsService {
    @GET("affiliate-dashboard/trading-volume-stats")
    suspend fun getTradingVolumeStats(): ApiResponse<TradingVolumeStatsResponse>

    @GET("affiliate-dashboard/points")
    suspend fun getPoints(): ApiResponse<PointsResponse>

}
package arkham.service

import arkham.response.TickerResponse
import com.skydoves.sandwich.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PublicService {
    @UnauthorizedCall
    @GET("public/ticker")
    suspend fun getTicker(@Query("symbol") symbol: String): ApiResponse<TickerResponse>

    @UnauthorizedCall
    @GET("public/tickers")
    suspend fun getTickers(): ApiResponse<List<TickerResponse>>
}
package com.antik.utils.arkham

import com.antik.utils.arkham.request.OrderRequest
import com.antik.utils.arkham.response.Balance
import com.antik.utils.arkham.response.OrderResponse
import com.antik.utils.arkham.response.UserResponse
import com.antik.utils.arkham.service.AccountService
import com.antik.utils.arkham.service.OrderService
import com.antik.utils.arkham.service.UserService
import arkham.response.PointsResponse
import arkham.response.TickerResponse
import arkham.response.TradingVolumeStatsResponse
import arkham.service.PublicService
import arkham.service.StatsService
import com.antik.utils.logger.Logger
import com.skydoves.sandwich.*
import com.skydoves.sandwich.retrofit.adapters.ApiResponseCallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ArkmClient(client: OkHttpClient, private val logger: Logger) {

    private companion object {
        private const val BASE_URL = "https://arkm.com/api/"
    }

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(createGson()))
        .addCallAdapterFactory(ApiResponseCallAdapterFactory.create())
        .build()

    private val userService: UserService = retrofit.create(UserService::class.java)
    private val orderService: OrderService = retrofit.create(OrderService::class.java)
    private val accountService: AccountService = retrofit.create(AccountService::class.java)
    private val publicService: PublicService = retrofit.create(PublicService::class.java)
    private val statsService: StatsService = retrofit.create(StatsService::class.java)

    suspend fun getUser(): UserResponse? {
        val response = userService.getUser()
        response.onFailure {
            logger.debug("ApiGetUser", message())
        }
        return response.getOrNull()
    }

    suspend fun createOrder(orderRequest: OrderRequest): OrderResponse? {
        val response = orderService.createOrder(orderRequest)
        response.onFailure {
            logger.debug("ApiCreateOrderError", message())
        }
        return response.getOrNull()
    }

    suspend fun getBalances(): List<Balance>? {
        val response = accountService.getBalances()
        response.onFailure {
            logger.debug("ApiGetBalancesError", message())
        }
        return response.getOrNull()
    }

    suspend fun getTicker(pair: String): TickerResponse? {
        val response = publicService.getTicker(pair)
        response.onFailure {
            logger.debug("ApiGetTickerError", message())
        }
        return response.getOrNull()
    }

    suspend fun getTradingVolumeStats(): TradingVolumeStatsResponse? {
        val response = statsService.getTradingVolumeStats()
        response.onFailure {
            logger.debug("ApiGetTradingVolumeStatsError", message())
        }
        return response.getOrNull()
    }

    suspend fun getPoints(): PointsResponse? {
        val response = statsService.getPoints()
        response.onFailure {
            logger.debug("ApiGetPointsError", message())
        }
        return response.getOrNull()
    }
}
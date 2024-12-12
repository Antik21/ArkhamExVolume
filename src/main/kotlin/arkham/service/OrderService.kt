package com.antik.utils.arkham.service

import com.antik.utils.arkham.request.OrderRequest
import com.antik.utils.arkham.response.OrderResponse
import retrofit2.http.Body
import retrofit2.http.POST
import com.skydoves.sandwich.ApiResponse

interface OrderService {
    @POST("orders/new")
    suspend fun createOrder(@Body orderRequest: OrderRequest): ApiResponse<OrderResponse>
}

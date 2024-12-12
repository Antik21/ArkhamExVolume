package com.antik.utils.arkham.service

import com.antik.utils.arkham.response.Balance
import com.skydoves.sandwich.ApiResponse
import retrofit2.http.GET

interface AccountService {
    @GET("account/balances/all")
    suspend fun getBalances(): ApiResponse<List<Balance>>
}
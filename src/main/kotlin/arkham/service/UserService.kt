package com.antik.utils.arkham.service

import com.antik.utils.arkham.response.UserResponse
import com.skydoves.sandwich.ApiResponse
import retrofit2.http.GET

interface UserService {
    @GET("user")
    suspend fun getUser(): ApiResponse<UserResponse>
}
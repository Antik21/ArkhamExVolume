package com.antik.utils.arkham

import arkham.serializer.OrderSideAdapter
import arkham.serializer.OrderTypeAdapter
import com.antik.utils.arkham.request.OrderSide
import com.antik.utils.arkham.request.OrderType
import com.google.gson.Gson
import com.google.gson.GsonBuilder

fun createGson(): Gson {
    return GsonBuilder()
        .registerTypeAdapter(OrderSide::class.java, OrderSideAdapter())
        .registerTypeAdapter(OrderType::class.java, OrderTypeAdapter())
        .create()
}
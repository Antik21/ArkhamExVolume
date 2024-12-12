package com.antik.utils.arkham.response

data class OrderResponse(
    val orderId: Long,
    val price: String,
    val side: String, // "buy" or "sell"
    val size: String,
    val subaccountId: Int,
    val time: Long, // Time in microseconds since UNIX epoch
    val type: String // "limitGtc", "limitIoc", "limitFok", "market"
)

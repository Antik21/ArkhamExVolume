package com.antik.utils.arkham.request

data class OrderRequest(
    val clientOrderId: String,
    val postOnly: Boolean = false,
    val price: String = "0",
    val side: OrderSide,
    val size: String,
    val subaccountId: Int = 0,
    val symbol: String,
    val type: OrderType
)
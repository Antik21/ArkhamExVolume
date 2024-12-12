package com.antik.utils.arkham.request

enum class OrderSide(val value: String) {
    BUY("buy"),
    SELL("sell");

    override fun toString(): String = value
}

enum class OrderType(val value: String) {
    LIMIT_GTC("limitGtc"),
    LIMIT_IOC("limitIoc"),
    LIMIT_FOK("limitFok"),
    MARKET("market");

    override fun toString(): String = value
}
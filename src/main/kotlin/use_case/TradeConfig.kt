package com.antik.utils.use_case

import arkham.token.Token

data class TradeConfig(
    val tokens: Set<Token>,
    val leverage: Int = 1,
    val waitBeforeSell: Int,
    val waitBetweenCycles: Int,
    val timeRange: Int,
    val maxVolume: Double,
    val minBalanceUSD: Double
)

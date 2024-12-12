package com.antik.utils.arkham.response

data class Balance(
    val balance: String,
    val balanceUSDT: String,
    val free: String,
    val freeUSDT: String,
    val lastUpdateReason: UpdateReason,
    val lastUpdateTime: Long,
    val priceUSDT: String,
    val subaccountId: Int,
    val symbol: String
)

enum class UpdateReason(val value: String) {
    DEPOSIT("deposit"),
    WITHDRAW("withdraw"),
    ORDER_FILL("orderFill"),
    FUNDING_FEE("fundingFee"),
    ASSET_TRANSFER("assetTransfer"),
    LIQUIDATION("liquidation"),
    REALIZE_PNL("realizePNL"),
    LSP_ASSIGNMENT("lspAssignment"),
    DELEVERAGE("deleverage"),
    TRADING_FEE("tradingFee"),
    REBATE("rebate"),
    COMMISSION("commission"),
    ADJUSTMENT("adjustment");

    override fun toString(): String = value
}

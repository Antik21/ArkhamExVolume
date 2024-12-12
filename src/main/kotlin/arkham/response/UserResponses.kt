package com.antik.utils.arkham.response

data class UserResponse(
    val becameVipAt: Long,
    val email: String,
    val id: Int,
    val kycVerifiedAt: Long,
    val pmm: Boolean,
    val requireMFA: Boolean,
    val settings: UserSettings,
    val subaccounts: List<Subaccount>,
    val username: String
)

data class UserSettings(
    val autogenDepositAddresses: Boolean,
    val confirmBeforePlaceOrder: Boolean,
    val hideBalances: Boolean,
    val marginUsageThreshold: Double,
    val notifyAnnouncements: Boolean,
    val notifyDeposits: Boolean,
    val notifyMarginUsage: Boolean,
    val notifyOrderFills: Boolean,
    val notifySendEmail: Boolean,
    val notifyWithdrawals: Boolean,
    val tickerTapeScroll: Boolean,
    val updatesFlash: Boolean
)

data class Subaccount(
    val futuresEnabled: Boolean,
    val id: Int,
    val isLsp: Boolean,
    val name: String,
    val payFeesInArkm: Boolean,
    val pinned: Boolean
)

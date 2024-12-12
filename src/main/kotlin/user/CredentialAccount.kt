package com.antik.utils.user

data class CredentialAccount(
    val accountId: String,
    val apiKey: String,
    val apiSecretKey: String,
    val proxy: Proxy? = null,
)

data class Proxy(
    val proxyIp: String,
    val proxyPort: String,
    val proxyLogin: String,
    val proxyPassword: String,
)
package user

import com.antik.utils.user.CredentialAccount
import com.antik.utils.user.Proxy

fun parseAccount(accountData: String): Result<CredentialAccount> {
    val parts = accountData.trim().split(":")
    if (parts.size != 3 && parts.size != 7) {
        return Result.failure(IllegalArgumentException("Invalid format in line: $accountData"))
    }

    val accountId = parts[0]
    val apiKey = parts[1]
    val apiSecretKey = parts[2]

    val proxy = if (parts.size == 7) {
        Proxy(
            proxyIp = parts[3],
            proxyPort = parts[4],
            proxyLogin = parts[5],
            proxyPassword = parts[6],
        )
    } else null

    return Result.success(
        CredentialAccount(
            accountId = accountId,
            apiKey = apiKey,
            apiSecretKey = apiSecretKey,
            proxy = proxy,
        )
    )
}
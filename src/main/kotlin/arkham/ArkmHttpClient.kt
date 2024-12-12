package com.antik.utils.arkham

import arkham.service.UnauthorizedCall
import com.antik.utils.user.CredentialAccount
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Invocation
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.*
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private const val BASE_PATH = "/api"

fun createOkHttpClient(user: CredentialAccount): OkHttpClient {
    val apiAuthInterceptor = createApiAuthInterceptor(user)
    val loggingInterceptor = createLoggingInterceptor()

    val okHttpBuilder = OkHttpClient.Builder()
        .addInterceptor(apiAuthInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)

    user.proxy?.let { proxyConfig ->
        val proxy = Proxy(
            Proxy.Type.HTTP,
            InetSocketAddress(proxyConfig.proxyIp, proxyConfig.proxyPort.toInt())
        )

        val proxyAuthenticator = { _: Route?, response: Response ->
            response.request.newBuilder()
                .header(
                    "Proxy-Authorization",
                    Credentials.basic(proxyConfig.proxyLogin, proxyConfig.proxyPassword)
                )
                .build()
        }

        okHttpBuilder
            .proxy(proxy)
            .proxyAuthenticator(proxyAuthenticator)
    }

    return okHttpBuilder.build()
}

private fun createApiAuthInterceptor(user: CredentialAccount): Interceptor {
    return Interceptor { chain ->
        val originalRequest = chain.request()
        val newRequestBuilder = originalRequest.newBuilder()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")

        if (!originalRequest.isUnauthorizedCall()) {
            val method = originalRequest.method
            val path = originalRequest.url.encodedPath.removePrefix(BASE_PATH)
            val body = originalRequest.body?.let { it.toUtf8String() } ?: ""

            val expires = (System.currentTimeMillis() / 1000 + 300) * 1_000_000
            val dataToSign = "${user.apiKey}$expires$method$path$body"
            val signature = generateHMACSignature(dataToSign, user.apiSecretKey)

            newRequestBuilder
                .header("Arkham-Api-Key", user.apiKey)
                .header("Arkham-Expires", expires.toString())
                .header("Arkham-Signature", signature)
        }

        chain.proceed(newRequestBuilder.build())
    }
}

private fun createLoggingInterceptor(): HttpLoggingInterceptor {
    return HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }
}

private fun RequestBody.toUtf8String(): String {
    val buffer = okio.Buffer()
    writeTo(buffer)
    return buffer.readUtf8()
}

private fun generateHMACSignature(data: String, secretKey: String): String {
    val hmacSHA256 = "HmacSHA256"
    val decodedKey = Base64.getDecoder().decode(secretKey)
    val secretKeySpec = SecretKeySpec(decodedKey, hmacSHA256)
    val mac = Mac.getInstance(hmacSHA256).apply { init(secretKeySpec) }
    val hash = mac.doFinal(data.toByteArray(Charsets.UTF_8))
    return Base64.getEncoder().encodeToString(hash)
}

private fun Request.isUnauthorizedCall(): Boolean {
    try {
        val annotations = tag(Invocation::class.java)?.method()?.annotations
        return annotations?.any { it is UnauthorizedCall } == true
    } catch (e: Exception) {
        return false
    }
}

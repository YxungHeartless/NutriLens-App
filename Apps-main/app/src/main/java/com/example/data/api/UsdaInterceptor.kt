package com.example.data.api

import okhttp3.Interceptor
import okhttp3.Response

class UsdaInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url

        // Append api_key query parameter to all requests going to USDA server
        return if (originalUrl.host.equals("api.nal.usda.gov", ignoreCase = true)) {
            val newUrl = originalUrl.newBuilder()
                .setQueryParameter("api_key", apiKey)
                .build()
            val newRequest = originalRequest.newBuilder()
                .url(newUrl)
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}

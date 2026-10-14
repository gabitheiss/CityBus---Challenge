package com.android.citybus.service

import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor(private val token: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val urlWithToken = original.url.newBuilder()
            .addQueryParameter("token", token)
            .build()
        val request = original.newBuilder()
            .url(urlWithToken)
            .build()
        return chain.proceed(request)
    }
}

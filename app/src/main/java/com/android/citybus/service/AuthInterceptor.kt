package com.android.citybus.service

import com.android.citybus.session.SessionManager
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class AuthInterceptor(private val session: SessionManager) : Interceptor {

    private val loginLock = Any()

    override fun intercept(chain: Interceptor.Chain): Response {
        val cookieAtRequest = session.cookie
        val response = chain.proceed(chain.request().withCookie(cookieAtRequest))
        if (response.code != HTTP_UNAUTHORIZED) return response

        val unauthorized = response.buffered()
        if (!renewSession(cookieAtRequest, chain)) return unauthorized

        unauthorized.close()
        return chain.proceed(chain.request().withCookie(session.cookie))
    }

    private fun renewSession(cookieAtRequest: String, chain: Interceptor.Chain): Boolean =
        synchronized(loginLock) {
            if (session.cookie != cookieAtRequest) return true

            chain.proceed(loginRequest(chain.request())).use { login ->
                val newCookie = login.header(HEADER_SET_COOKIE)
                val authenticated =
                    login.isSuccessful && login.body?.string()?.trim() == LOGIN_SUCCESS_BODY
                if (!authenticated || newCookie == null) return@use false

                session.cookie = newCookie
                true
            }
        }

    private fun loginRequest(original: Request): Request =
        Request.Builder()
            .url(original.url.newBuilder().encodedPath(LOGIN_PATH).query(null).build())
            .post(EMPTY_BODY.toRequestBody())
            .build()

    private fun Request.withCookie(cookie: String): Request =
        if (cookie.isEmpty()) this else newBuilder().header(HEADER_COOKIE, cookie).build()

    private fun Response.buffered(): Response {
        val body = body ?: return this
        val contentType = body.contentType()
        return newBuilder().body(body.bytes().toResponseBody(contentType)).build()
    }

    private companion object {
        const val LOGIN_PATH = "/v2.1/Login/Autenticar"
        const val HEADER_COOKIE = "Cookie"
        const val HEADER_SET_COOKIE = "set-cookie"
        const val LOGIN_SUCCESS_BODY = "true"
        const val EMPTY_BODY = ""
    }
}

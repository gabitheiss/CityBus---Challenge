package com.android.citybus.service

import com.android.citybus.session.SessionManagerImpl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.QueueDispatcher
import okhttp3.mockwebserver.RecordedRequest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AuthInterceptorTest {

    private val server = MockWebServer()
    private val session = SessionManagerImpl()

    private lateinit var client: OkHttpClient

    @Before
    fun setUp() {
        (server.dispatcher as QueueDispatcher).setFailFast(true)
        server.start()
        client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(session))
            .build()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `attaches the session cookie and returns the response when it is not 401`() {
        session.cookie = "apiCredentials=abc"
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        val response = client.newCall(requestTo("/v2.1/Linha/Buscar")).execute()

        assertEquals(200, response.code)
        assertEquals(1, server.requestCount)
        assertEquals("apiCredentials=abc", server.takeRequest().getHeader("Cookie"))
    }

    @Test
    fun `renews the session and retries the request once when the response is 401`() {
        session.cookie = "apiCredentials=old"
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Set-Cookie", "apiCredentials=new")
                .setBody("true")
        )
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        val response = client.newCall(requestTo("/v2.1/Linha/Buscar")).execute()

        assertEquals(200, response.code)
        assertEquals("apiCredentials=new", session.cookie)
        assertEquals(3, server.requestCount)

        assertEquals("apiCredentials=old", server.takeRequest().getHeader("Cookie"))
        val login = server.takeRequest()
        assertEquals("POST", login.method)
        assertEquals("/v2.1/Login/Autenticar", login.path)
        assertEquals("apiCredentials=new", server.takeRequest().getHeader("Cookie"))
    }

    @Test
    fun `returns the original 401 without retrying when the login responds with an error`() {
        session.cookie = "apiCredentials=old"
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setResponseCode(500))

        val response = client.newCall(requestTo("/v2.1/Linha/Buscar")).execute()

        assertEquals(401, response.code)
        assertEquals("apiCredentials=old", session.cookie)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `keeps the body of the original 401 readable when the login fails`() {
        server.enqueue(MockResponse().setResponseCode(401).setBody("nao autorizado"))
        server.enqueue(MockResponse().setResponseCode(500))

        val response = client.newCall(requestTo("/v2.1/Posicao")).execute()

        assertEquals("nao autorizado", response.body?.string())
    }

    @Test
    fun `returns the original 401 without retrying when the login body is not true`() {
        session.cookie = "apiCredentials=old"
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Set-Cookie", "apiCredentials=new")
                .setBody("false")
        )

        val response = client.newCall(requestTo("/v2.1/Posicao")).execute()

        assertEquals(401, response.code)
        assertEquals("apiCredentials=old", session.cookie)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `logs in once when two concurrent requests get a 401`() {
        val logins = AtomicInteger()
        val bothUnauthorized = CountDownLatch(2)
        session.cookie = "apiCredentials=old"
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = when {
                request.path.orEmpty().startsWith("/v2.1/Login/Autenticar") -> {
                    logins.incrementAndGet()
                    MockResponse()
                        .setResponseCode(200)
                        .addHeader("Set-Cookie", "apiCredentials=new")
                        .setBody("true")
                }

                request.getHeader("Cookie") == "apiCredentials=new" ->
                    MockResponse().setResponseCode(200).setBody("[]")

                else -> {
                    bothUnauthorized.countDown()
                    bothUnauthorized.await(5, TimeUnit.SECONDS)
                    MockResponse().setResponseCode(401)
                }
            }
        }

        val pool = Executors.newFixedThreadPool(2)
        val codes = try {
            (1..2)
                .map { pool.submit<Int> { client.newCall(requestTo("/v2.1/Posicao")).execute().use { it.code } } }
                .map { it.get(10, TimeUnit.SECONDS) }
        } finally {
            pool.shutdown()
        }

        assertEquals(listOf(200, 200), codes)
        assertEquals(1, logins.get())
        assertEquals("apiCredentials=new", session.cookie)
    }

    @Test
    fun `does not send a cookie header when there is no session yet`() {
        server.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

        client.newCall(requestTo("/v2.1/Posicao")).execute()

        assertNull(server.takeRequest().getHeader("Cookie"))
    }

    @Test
    fun `returns the original 401 without retrying when the login sends no cookie`() {
        session.cookie = "apiCredentials=old"
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setResponseCode(200).setBody("true"))

        val response = client.newCall(requestTo("/v2.1/Posicao")).execute()

        assertEquals(401, response.code)
        assertEquals("apiCredentials=old", session.cookie)
        assertEquals(2, server.requestCount)
    }

    private fun requestTo(path: String): Request =
        Request.Builder().url(server.url(path)).build()
}

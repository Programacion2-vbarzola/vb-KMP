package com.vbarzola.shared.network

import com.vbarzola.shared.auth.TokenStorage
import com.vbarzola.shared.network.dto.AccountDto
import com.vbarzola.shared.network.dto.LoginRequest
import com.vbarzola.shared.network.dto.RegisterRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.statement.bodyAsText
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class TurnosApiClient(
    baseUrl: String,
    private val tokenStorage: TokenStorage
) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(Logging) {
            level = LogLevel.HEADERS
        }
        defaultRequest {
            url(baseUrl)
        }
    }

    suspend fun login(username: String, password: String): Result<String> = runCatching {
        val response = client.post("/api/authenticate") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username, password))
        }
        if (!response.status.isSuccess()) error("Login failed: ${response.status.value}")
        val raw = response.bodyAsText()
        val json = Json { ignoreUnknownKeys = true }
        json.parseToJsonElement(raw).jsonObject["id_token"]?.jsonPrimitive?.content
            ?: error("Respuesta inesperada del servidor: $raw")
    }

    suspend fun register(request: RegisterRequest): Result<Unit> = runCatching {
        val response = client.post("/api/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (!response.status.isSuccess()) error("Register failed: ${response.status.value}")
    }

    suspend fun getAccount(): Result<AccountDto> = runCatching {
        val response = client.get("/api/account") {
            bearerToken()
        }
        if (!response.status.isSuccess()) error("Unauthorized")
        response.body<AccountDto>()
    }

    private fun io.ktor.client.request.HttpRequestBuilder.bearerToken() {
        val token = tokenStorage.getToken() ?: error("No token stored")
        header(HttpHeaders.Authorization, "Bearer $token")
    }
}

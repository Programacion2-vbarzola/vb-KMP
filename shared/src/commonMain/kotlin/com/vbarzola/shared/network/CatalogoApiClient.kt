package com.vbarzola.shared.network

import com.vbarzola.shared.auth.TokenStorage
import com.vbarzola.shared.network.dto.CatalogCategoryDto
import com.vbarzola.shared.network.dto.CatalogProfessionalDto
import com.vbarzola.shared.network.dto.CatalogWeeklyScheduleDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class CatalogoApiClient(
    baseUrl: String,
    private val tokenStorage: TokenStorage
) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
        install(Logging) {
            level = LogLevel.HEADERS
        }
        defaultRequest {
            url(baseUrl)
        }
    }

    suspend fun getCategories(): Result<List<CatalogCategoryDto>> = runCatching {
        client.get("/api/professional-categories?size=1000&sort=name,asc") {
            bearerToken()
        }.body<List<CatalogCategoryDto>>()
    }

    suspend fun getProfessionals(): Result<List<CatalogProfessionalDto>> = runCatching {
        client.get("/api/professionals?size=1000&sort=lastName,asc&eagerload=true") {
            bearerToken()
        }.body<List<CatalogProfessionalDto>>()
    }

    suspend fun getWeeklySchedules(): Result<List<CatalogWeeklyScheduleDto>> = runCatching {
        client.get("/api/weekly-schedules?size=1000&sort=id,asc&eagerload=true") {
            bearerToken()
        }.body<List<CatalogWeeklyScheduleDto>>()
    }

    private fun io.ktor.client.request.HttpRequestBuilder.bearerToken() {
        val token = tokenStorage.getToken() ?: error("No hay sesión activa")
        header(HttpHeaders.Authorization, "Bearer $token")
    }
}

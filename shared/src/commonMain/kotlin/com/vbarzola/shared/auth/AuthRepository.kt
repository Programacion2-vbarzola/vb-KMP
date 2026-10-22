package com.vbarzola.shared.auth

import com.vbarzola.shared.network.TurnosApiClient
import com.vbarzola.shared.network.dto.RegisterRequest

class AuthRepository(
    private val apiClient: TurnosApiClient,
    private val tokenStorage: TokenStorage
) {
    fun isLoggedIn(): Boolean = tokenStorage.hasToken()

    suspend fun login(username: String, password: String): Result<Unit> {
        return apiClient.login(username, password).map { token ->
            tokenStorage.saveToken(token)
        }
    }

    suspend fun register(
        login: String,
        password: String,
        firstName: String,
        lastName: String,
        email: String
    ): Result<Unit> = apiClient.register(
        RegisterRequest(login, password, firstName, lastName, email)
    )

    fun logout() = tokenStorage.clearToken()
}

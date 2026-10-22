package com.vbarzola.shared.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
    val rememberMe: Boolean = true
)

@Serializable
data class LoginResponse(
    @SerialName("id_token") val idToken: String
)

@Serializable
data class RegisterRequest(
    val login: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val langKey: String = "es"
)

@Serializable
data class AccountDto(
    val login: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null
)

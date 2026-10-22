package com.vbarzola.shared.auth

interface TokenStorage {
    fun saveToken(token: String)
    fun getToken(): String?
    fun clearToken()
    fun hasToken(): Boolean = getToken() != null
}

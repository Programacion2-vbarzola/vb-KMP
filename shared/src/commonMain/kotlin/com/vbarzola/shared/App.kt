package com.vbarzola.shared

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.vbarzola.shared.auth.AuthRepository
import com.vbarzola.shared.auth.AuthViewModel
import com.vbarzola.shared.auth.TokenStorage
import com.vbarzola.shared.network.TurnosApiClient
import com.vbarzola.shared.ui.home.HomeScreen
import com.vbarzola.shared.ui.login.LoginScreen
import com.vbarzola.shared.ui.register.RegisterScreen

private sealed class Screen {
    object Login : Screen()
    object Register : Screen()
    object Home : Screen()
}

@Composable
fun App(tokenStorage: TokenStorage, turnosBaseUrl: String) {
    val scope = rememberCoroutineScope()
    val apiClient = remember { TurnosApiClient(turnosBaseUrl, tokenStorage) }
    val authRepository = remember { AuthRepository(apiClient, tokenStorage) }
    val authViewModel = remember { AuthViewModel(authRepository, scope) }

    var screen by remember {
        mutableStateOf(if (authRepository.isLoggedIn()) Screen.Home else Screen.Login)
    }

    MaterialTheme {
        Surface {
            when (screen) {
                is Screen.Login -> LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = { screen = Screen.Home },
                    onNavigateToRegister = {
                        authViewModel.resetState()
                        screen = Screen.Register
                    }
                )
                is Screen.Register -> RegisterScreen(
                    viewModel = authViewModel,
                    onRegisterSuccess = { screen = Screen.Login },
                    onNavigateToLogin = {
                        authViewModel.resetState()
                        screen = Screen.Login
                    }
                )
                is Screen.Home -> HomeScreen(
                    onLogout = {
                        authRepository.logout()
                        screen = Screen.Login
                    }
                )
            }
        }
    }
}

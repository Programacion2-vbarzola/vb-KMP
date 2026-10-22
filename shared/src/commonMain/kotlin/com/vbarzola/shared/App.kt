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
import com.vbarzola.shared.catalog.CatalogRepository
import com.vbarzola.shared.catalog.CatalogViewModel
import com.vbarzola.shared.db.AppDatabase
import com.vbarzola.shared.db.DatabaseDriverFactory
import com.vbarzola.shared.network.CatalogoApiClient
import com.vbarzola.shared.network.TurnosApiClient
import com.vbarzola.shared.ui.home.HomeScreen
import com.vbarzola.shared.ui.login.LoginScreen
import com.vbarzola.shared.ui.register.RegisterScreen
import com.vbarzola.shared.ui.search.SearchScreen

private sealed class Screen {
    object Login : Screen()
    object Register : Screen()
    object Home : Screen()
    object Search : Screen()
}

@Composable
fun App(
    tokenStorage: TokenStorage,
    turnosBaseUrl: String,
    catalogoBaseUrl: String,
    driverFactory: DatabaseDriverFactory
) {
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase(driverFactory.create()) }

    val turnosApiClient = remember { TurnosApiClient(turnosBaseUrl, tokenStorage) }
    val catalogoApiClient = remember { CatalogoApiClient(catalogoBaseUrl, tokenStorage) }

    val authRepository = remember { AuthRepository(turnosApiClient, tokenStorage) }
    val catalogRepository = remember { CatalogRepository(database, catalogoApiClient) }
    val authViewModel = remember { AuthViewModel(authRepository, scope) }
    val catalogViewModel = remember { CatalogViewModel(catalogRepository, scope) }

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
                    catalogRepository = catalogRepository,
                    scope = scope,
                    onLogout = {
                        authRepository.logout()
                        screen = Screen.Login
                    },
                    onNavigateToSearch = { screen = Screen.Search }
                )
                is Screen.Search -> SearchScreen(
                    viewModel = catalogViewModel,
                    onBack = { screen = Screen.Home },
                    onSelectProfessional = { _ ->
                        // Sprint 3: navegar a disponibilidad del profesional
                    }
                )
            }
        }
    }
}

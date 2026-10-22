package com.vbarzola.turnos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.vbarzola.shared.App
import com.vbarzola.shared.auth.AndroidTokenStorage
import com.vbarzola.shared.db.AndroidDatabaseDriverFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tokenStorage = AndroidTokenStorage(applicationContext)
        val driverFactory = AndroidDatabaseDriverFactory(applicationContext)
        setContent {
            App(
                tokenStorage = tokenStorage,
                turnosBaseUrl = BuildConfig.TURNOS_BASE_URL,
                catalogoBaseUrl = BuildConfig.CATALOGO_BASE_URL,
                driverFactory = driverFactory
            )
        }
    }
}

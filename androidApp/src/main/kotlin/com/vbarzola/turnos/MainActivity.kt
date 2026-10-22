package com.vbarzola.turnos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.vbarzola.shared.App
import com.vbarzola.shared.auth.AndroidTokenStorage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tokenStorage = AndroidTokenStorage(applicationContext)
        setContent {
            App(
                tokenStorage = tokenStorage,
                turnosBaseUrl = BuildConfig.TURNOS_BASE_URL
            )
        }
    }
}

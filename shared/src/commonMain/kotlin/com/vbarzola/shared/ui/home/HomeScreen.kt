package com.vbarzola.shared.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vbarzola.shared.catalog.CatalogRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    catalogRepository: CatalogRepository,
    scope: CoroutineScope,
    onLogout: () -> Unit,
    onNavigateToSearch: () -> Unit = {}
) {
    var syncState by remember { mutableStateOf<SyncState>(SyncState.Idle) }

    LaunchedEffect(Unit) {
        if (!catalogRepository.isSynced()) {
            syncState = SyncState.Syncing
            catalogRepository.syncSnapshot()
                .onSuccess { syncState = SyncState.Done }
                .onFailure { syncState = SyncState.Error(it.message ?: "Error al sincronizar") }
        } else {
            syncState = SyncState.Done
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Bienvenido", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        when (val s = syncState) {
            is SyncState.Idle -> {}
            is SyncState.Syncing -> {
                CircularProgressIndicator()
                Spacer(Modifier.height(8.dp))
                Text("Sincronizando catálogo...", style = MaterialTheme.typography.bodyMedium)
            }
            is SyncState.Done -> {
                Text(
                    "Catálogo listo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = onNavigateToSearch) {
                    Text("Buscar profesionales")
                }
            }
            is SyncState.Error -> {
                Text(
                    s.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    scope.launch {
                        syncState = SyncState.Syncing
                        catalogRepository.syncSnapshot()
                            .onSuccess { syncState = SyncState.Done }
                            .onFailure { syncState = SyncState.Error(it.message ?: "Error") }
                    }
                }) {
                    Text("Reintentar")
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        TextButton(onClick = onLogout) {
            Text("Cerrar sesión")
        }
    }
}

private sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    object Done : SyncState()
    data class Error(val message: String) : SyncState()
}

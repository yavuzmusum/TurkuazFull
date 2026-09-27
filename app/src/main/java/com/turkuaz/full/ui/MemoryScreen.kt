package com.turkuaz.full.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.turkuaz.full.data.ApiClient
import com.turkuaz.full.data.UserMemoryItem
import kotlinx.coroutines.launch

/**
 * Sadelestirilmis hafiza ekrani (Beta'daki key/value giris formu ve disa
 * aktarma YOK - sıradan kullanici icin sadece goruntule + sil).
 */
@Composable
fun MemoryScreen(apiClient: ApiClient) {
    val items = remember { mutableStateListOf<UserMemoryItem>() }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    suspend fun reload() {
        try {
            val list = apiClient.getUserMemory()
            items.clear()
            items.addAll(list)
        } catch (e: Exception) {
            statusMessage = "Yükleme hatası: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { reload() }

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Text(
            "Hakkınızda bildiklerimiz",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        when {
            isLoading -> {}
            items.isEmpty() -> Text(
                "Şu an sizinle ilgili kayıtlı bir bilgi yok.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            else -> LazyColumn(Modifier.weight(1f)) {
                items(items, key = { it.id }) { item ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(
                            Modifier.fillMaxWidth().padding(10.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        ) {
                            Text(item.value, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                            IconButton(onClick = {
                                scope.launch {
                                    try {
                                        apiClient.deleteUserMemory(item.id)
                                        reload()
                                    } catch (e: Exception) {
                                        statusMessage = "Silme hatası: ${e.message}"
                                    }
                                }
                            }) { Icon(Icons.Default.Delete, contentDescription = "Sil") }
                        }
                    }
                }
            }
        }

        statusMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}

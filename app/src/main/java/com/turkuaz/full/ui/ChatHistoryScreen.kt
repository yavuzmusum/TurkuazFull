package com.turkuaz.full.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.turkuaz.full.data.ChatHistoryStore
import java.text.DateFormat
import java.util.Date

@Composable
fun ChatHistoryScreen(historyStore: ChatHistoryStore, onOpenConversation: (String) -> Unit) {
    val conversations = remember { historyStore.getAll() }
    val dateFormat = remember { DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT) }

    if (conversations.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text(
                "Henüz bir sohbet geçmişiniz yok.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    LazyColumn(Modifier.fillMaxSize().padding(12.dp)) {
        items(conversations, key = { it.id }) { conversation ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                onClick = { onOpenConversation(conversation.id) },
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        conversation.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        dateFormat.format(Date(conversation.updatedAt)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

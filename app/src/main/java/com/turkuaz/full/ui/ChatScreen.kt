package com.turkuaz.full.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.turkuaz.full.data.ApiClient
import com.turkuaz.full.data.ChatHistoryStore
import com.turkuaz.full.data.ChatLine
import com.turkuaz.full.data.Conversation
import com.turkuaz.full.data.makeConversationTitle
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Ana ekran. Beta'dan farkli olarak: alt sekme yok, tek sohbet ekrani.
 * Her konusma otomatik olarak (yerel) gecmise kaydediliyor - bkz.
 * ChatHistoryStore.
 *
 * "sessionKey" ONEMLI: mesaj listesi buna gore remember ediliyor, ilk
 * mesajda kendi kendine atanan conversationId'ye gore DEGIL. Cunku
 * conversationId, AppRoot'a bildirilip oradan geri "initialConversationId"
 * olarak akip tekrar bu composable'a giriyor; eger remember direkt ona
 * bagli olsaydi, tam da ilk mesaji gonderdiginiz an ekrandaki liste
 * "farkli bir sohbete gecildi" sanilip sifirlanirdi (yasanan hata buydu).
 * sessionKey sadece AppRoot'taki gercek navigasyon eylemlerinde (Yeni
 * Sohbet butonu, Gecmis Sohbetler'den bir kayit acma) artiyor.
 */
@Composable
fun ChatScreen(
    apiClient: ApiClient,
    historyStore: ChatHistoryStore,
    sessionKey: Int,
    initialConversationId: String?,
    onActiveConversationIdChange: (String) -> Unit,
) {
    var input by remember { mutableStateOf("") }
    var isBusy by remember { mutableStateOf(false) }
    var conversationId by remember(sessionKey) { mutableStateOf(initialConversationId) }
    val messages = remember(sessionKey) {
        val initial = initialConversationId?.let { historyStore.get(it)?.messages } ?: emptyList()
        mutableStateListOf<ChatLine>().apply { addAll(initial) }
    }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        if (messages.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text(
                    "Merhaba! Bir şey sorarak başlayabilirsiniz.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(state = listState, modifier = Modifier.weight(1f)) {
                items(messages) { line ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (line.fromUser)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    ) {
                        Text(line.text, Modifier.padding(10.dp))
                    }
                }
            }
        }

        Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
            OutlinedTextField(
                value = input, onValueChange = { input = it },
                placeholder = { Text("Bir şey sor...") },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            Spacer(Modifier.width(8.dp))
            Button(
                enabled = !isBusy && input.isNotBlank(),
                onClick = {
                    val msg = input
                    input = ""
                    val wasEmpty = messages.isEmpty()
                    messages.add(ChatLine(fromUser = true, text = msg))
                    isBusy = true

                    // Bu sohbetin ilk mesajiysa burada bir id atiyoruz. Bu
                    // atama "conversationId" (sessionKey'e bagli, ayri bir
                    // state) uzerinde oldugu icin "messages" listesini
                    // ETKILEMIYOR - liste sadece sessionKey degisince
                    // sifirlanir.
                    val idForThisConversation = conversationId ?: UUID.randomUUID().toString().also {
                        conversationId = it
                        onActiveConversationIdChange(it)
                    }

                    scope.launch {
                        try {
                            val reply = apiClient.sendChat(msg)
                            messages.add(ChatLine(fromUser = false, text = reply.reply))
                        } catch (e: Exception) {
                            messages.add(ChatLine(fromUser = false, text = "Hata: ${e.message}"))
                        } finally {
                            isBusy = false
                            val title = if (wasEmpty) makeConversationTitle(msg)
                                        else historyStore.get(idForThisConversation)?.title ?: makeConversationTitle(msg)
                            historyStore.save(
                                Conversation(
                                    id = idForThisConversation,
                                    title = title,
                                    updatedAt = System.currentTimeMillis(),
                                    messages = messages.toList(),
                                )
                            )
                            if (messages.isNotEmpty()) {
                                listState.animateScrollToItem(messages.size - 1)
                            }
                        }
                    }
                },
            ) { Text("Gönder") }
        }
    }
}

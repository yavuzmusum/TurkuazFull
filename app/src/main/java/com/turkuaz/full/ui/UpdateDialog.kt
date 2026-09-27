package com.turkuaz.full.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.turkuaz.full.data.AppVersionInfo

/**
 * Bolum: uygulama ici guncelleme penceresi. force_update true ise geri
 * tusu/disari tiklama ile kapatilamaz - kullanici guncellemeden devam
 * edemez.
 */
@Composable
fun UpdateDialog(info: AppVersionInfo, onDismiss: () -> Unit, onUpdateClick: () -> Unit) {
    var isDownloading by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!info.forceUpdate) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !info.forceUpdate,
            dismissOnClickOutside = !info.forceUpdate,
        ),
        title = { Text(if (info.forceUpdate) "Güncelleme Gerekli" else "Yeni Sürüm Mevcut") },
        text = {
            Column {
                Text("Sürüm ${info.versionName}", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                if (info.releaseNotes.isNotBlank()) {
                    Text(info.releaseNotes, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                }
                if (info.forceUpdate) {
                    Text(
                        "Devam etmek için güncellemeniz gerekiyor.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isDownloading,
                onClick = {
                    isDownloading = true
                    onUpdateClick()
                },
            ) {
                Text(if (isDownloading) "İndiriliyor..." else "Güncelle")
            }
        },
        dismissButton = {
            if (!info.forceUpdate) {
                TextButton(onClick = onDismiss) { Text("Daha Sonra") }
            }
        },
    )
}

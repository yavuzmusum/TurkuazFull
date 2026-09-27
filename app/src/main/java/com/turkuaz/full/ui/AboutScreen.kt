package com.turkuaz.full.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.turkuaz.full.data.Config

@Composable
fun AboutScreen() {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp))
        Text("TURKUAZ AI", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text(
            "TURKUAZ AI, Türkçe konuşan, sizi hatırlayan ve sizden öğrendiklerini " +
                "bir yöneticinin onayından geçirdikten sonra paylaşılan bilgiye " +
                "dönüştüren bir yapay zeka asistanıdır.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "\"Bizi biz yapan sizlersiniz.\"",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.weight(1f))
        Text(
            "Sürüm ${Config.APP_VERSION}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
    }
}

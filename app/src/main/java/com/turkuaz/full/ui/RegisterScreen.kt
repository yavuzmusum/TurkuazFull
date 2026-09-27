package com.turkuaz.full.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.turkuaz.full.data.ApiClient
import kotlinx.coroutines.launch

/**
 * Bolum 2.2 kabul kriteri: "kayit/giris ... uctan uca calisiyor". Sunucudaki
 * /api/v1/auth/register ucu zaten token dondurdugu icin basarili kayittan
 * sonra ayrica giris ekranina gerek yok - dogrudan ana ekrana geciliyor.
 */
@Composable
fun RegisterScreen(apiClient: ApiClient, onRegistered: (role: String) -> Unit, onBackToLogin: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isBusy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(Modifier.fillMaxWidth()) {
            Text("Kayıt Ol", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Bizi biz yapan sizlersiniz.",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = username, onValueChange = { username = it },
                label = { Text("Kullanıcı adı") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Şifre") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    error = null
                    isBusy = true
                    scope.launch {
                        try {
                            val token = apiClient.register(username, password)
                            onRegistered(token.role)
                        } catch (e: Exception) {
                            error = e.message ?: "Kayıt başarısız"
                        } finally {
                            isBusy = false
                        }
                    }
                },
                enabled = !isBusy && username.isNotBlank() && password.length >= 6,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isBusy) "Kayıt yapılıyor..." else "Kayıt Ol")
            }

            if (password.isNotEmpty() && password.length < 6) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Şifre en az 6 karakter olmalı.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            TextButton(onClick = onBackToLogin, modifier = Modifier.fillMaxWidth()) {
                Text("Zaten hesabınız var mı? Giriş yapın")
            }

            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

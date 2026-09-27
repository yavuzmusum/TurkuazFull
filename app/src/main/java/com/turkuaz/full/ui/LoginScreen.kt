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

@Composable
fun LoginScreen(apiClient: ApiClient, onLoggedIn: (role: String) -> Unit, onGoToRegister: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isBusy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(Modifier.fillMaxWidth()) {
            Text("TURKUAZ AI", style = MaterialTheme.typography.headlineMedium)
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
                            val token = apiClient.login(username, password)
                            onLoggedIn(token.role)
                        } catch (e: Exception) {
                            error = e.message ?: "Giriş başarısız"
                        } finally {
                            isBusy = false
                        }
                    }
                },
                enabled = !isBusy && username.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isBusy) "Giriş yapılıyor..." else "Giriş Yap")
            }

            error?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            TextButton(onClick = onGoToRegister, modifier = Modifier.fillMaxWidth()) {
                Text("Hesabınız yok mu? Kayıt olun")
            }
        }
    }
}

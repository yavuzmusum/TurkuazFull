package com.turkuaz.full

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.turkuaz.full.data.ApiClient
import com.turkuaz.full.data.AppVersionInfo
import com.turkuaz.full.data.ApkUpdater
import com.turkuaz.full.data.ChatHistoryStore
import com.turkuaz.full.data.Config
import com.turkuaz.full.data.DeviceId
import com.turkuaz.full.data.SessionStore
import com.turkuaz.full.ui.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val session = SessionStore(applicationContext)
        val fingerprint = DeviceId.get(applicationContext)
        val apiClient = ApiClient(session, fingerprint)
        val historyStore = ChatHistoryStore(applicationContext)

        setContent {
            TurkuazTheme {
                AppRoot(apiClient = apiClient, session = session, historyStore = historyStore)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppRoot(apiClient: ApiClient, session: SessionStore, historyStore: ChatHistoryStore) {
    var isLoggedIn by remember { mutableStateOf(session.isLoggedIn) }
    var showRegister by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Chat) }
    var activeConversationId by remember { mutableStateOf<String?>(null) }
    // ChatScreen'in mesaj listesi buna gore "remember" ediliyor. Sadece
    // gercekten yeni bir sohbete GECILDIGINDE (asagidaki iki durumda)
    // artiyor - ChatScreen'in ilk mesajda kendi kendine bir conversationId
    // atamasi bunu ARTIRMAZ, aksi halde o an ekrandaki mesaj listesi
    // sifirlanir (yasanan hata buydu: ilk mesaj gonderilince ekran
    // "Merhaba..." haline donup mesaj sadece gecmise kaydoluyordu).
    var chatSessionKey by remember { mutableIntStateOf(0) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Bolum: uygulama ici guncelleme. Giris yapilmis olsun ya da olmasin,
    // acilista bir kere kontrol edilir (public /api/v1/app/version ucu).
    var updateInfo by remember { mutableStateOf<AppVersionInfo?>(null) }
    LaunchedEffect(Unit) {
        try {
            val info = apiClient.getAppVersion(Config.PACKAGE_NAME)
            if (info.versionCode > BuildConfig.VERSION_CODE) {
                updateInfo = info
            }
        } catch (e: Exception) {
            // Sunucuda bu paket icin kayitli surum yoksa (404) ya da ag
            // hatasi olursa: sessizce gec, uygulama normal calismaya devam
            // etmeli (asla guncelleme kontrolu yuzunden acilmamali).
        }
    }

    updateInfo?.let { info ->
        UpdateDialog(
            info = info,
            onDismiss = { updateInfo = null },
            onUpdateClick = {
                val started = ApkUpdater.startDownloadAndInstall(context, info.apkUrl, info.versionName)
                if (!started) {
                    // Izin ekranina yonlendirildi; dialogu kapatip kullanicinin
                    // "Guncelle"ye tekrar basmasina izin ver.
                    updateInfo = null
                }
            },
        )
    }

    if (!isLoggedIn) {
        if (showRegister) {
            RegisterScreen(
                apiClient = apiClient,
                onRegistered = { _ -> isLoggedIn = true },
                onBackToLogin = { showRegister = false },
            )
        } else {
            LoginScreen(
                apiClient = apiClient,
                onLoggedIn = { _ -> isLoggedIn = true },
                onGoToRegister = { showRegister = true },
            )
        }
        return
    }

    fun navigateTo(screen: AppScreen) {
        currentScreen = screen
        scope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(Modifier.padding(16.dp)) {
                    Text("TURKUAZ AI", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Bizi biz yapan sizlersiniz.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                HorizontalDivider()

                NavigationDrawerItem(
                    label = { Text("Sohbet") },
                    selected = currentScreen == AppScreen.Chat,
                    onClick = { navigateTo(AppScreen.Chat) },
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
                NavigationDrawerItem(
                    label = { Text("Geçmiş Sohbetler") },
                    selected = currentScreen == AppScreen.History,
                    onClick = { navigateTo(AppScreen.History) },
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
                NavigationDrawerItem(
                    label = { Text("Hafızam") },
                    selected = currentScreen == AppScreen.Memory,
                    onClick = { navigateTo(AppScreen.Memory) },
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
                NavigationDrawerItem(
                    label = { Text("Geri Bildirim") },
                    selected = currentScreen == AppScreen.Feedback,
                    onClick = { navigateTo(AppScreen.Feedback) },
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
                NavigationDrawerItem(
                    label = { Text("Hakkımızda") },
                    selected = currentScreen == AppScreen.About,
                    onClick = { navigateTo(AppScreen.About) },
                    modifier = Modifier.padding(horizontal = 12.dp),
                )

                Spacer(Modifier.weight(1f))
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("Çıkış Yap") },
                    selected = false,
                    onClick = {
                        session.clear()
                        isLoggedIn = false
                        showRegister = false
                        currentScreen = AppScreen.Chat
                        activeConversationId = null
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentScreen.title) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menü")
                        }
                    },
                    actions = {
                        if (currentScreen == AppScreen.Chat) {
                            IconButton(onClick = {
                                activeConversationId = null
                                chatSessionKey++
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Yeni Sohbet")
                            }
                        }
                    },
                )
            },
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                when (currentScreen) {
                    AppScreen.Chat -> ChatScreen(
                        apiClient = apiClient,
                        historyStore = historyStore,
                        sessionKey = chatSessionKey,
                        initialConversationId = activeConversationId,
                        onActiveConversationIdChange = { activeConversationId = it },
                    )
                    AppScreen.History -> ChatHistoryScreen(historyStore) { id ->
                        activeConversationId = id
                        chatSessionKey++
                        currentScreen = AppScreen.Chat
                    }
                    AppScreen.Memory -> MemoryScreen(apiClient)
                    AppScreen.Feedback -> FeedbackScreen(apiClient)
                    AppScreen.About -> AboutScreen()
                }
            }
        }
    }
}

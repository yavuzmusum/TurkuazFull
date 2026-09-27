package com.turkuaz.full.data

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import java.io.File

/**
 * APK indirme + Android paket yoneticisiyle otomatik kurulum ekranini acma.
 *
 * Ekstra kutuphane YOK: indirme icin Android'in kendi sistem servisi olan
 * DownloadManager, kurulum icin FileProvider (androidx.core - zaten
 * bagimlilik olarak var) kullanildi.
 *
 * NOT: Bu kod bu sandbox'ta gercek bir cihaz/emulator olmadigi icin
 * calistirilip test edilemedi. Ozellikle:
 *   - "Bilinmeyen kaynaklardan yukleme" izni akisi (Android 8+)
 *   - DownloadManager -> BroadcastReceiver -> kurulum Intent'i zinciri
 * ilk gercek testi sizin cihazinizda/emulatorunuzde yapmaniz gerekiyor.
 */
object ApkUpdater {
    private const val APK_FILE_NAME = "turkuaz-update.apk"

    /**
     * @return true ise indirme baslatildi; false ise "bilinmeyen kaynaklar"
     * izni istemek icin Ayarlar'a yonlendirildi - kullanici izni verip
     * geri donduginde "Guncelle"ye tekrar basmasi gerekir (MVP: otomatik
     * retry yok).
     */
    fun startDownloadAndInstall(context: Context, apkUrl: String, versionName: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
        ) {
            val settingsIntent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}"),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(settingsIntent)
            return false
        }

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val apkFile = File(downloadsDir, APK_FILE_NAME)
        if (apkFile.exists()) apkFile.delete()

        val request = DownloadManager.Request(Uri.parse(apkUrl))
            .setTitle("TURKUAZ AI güncelleniyor")
            .setDescription("Sürüm $versionName indiriliyor")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationUri(Uri.fromFile(apkFile))
            .setMimeType("application/vnd.android.package-archive")

        val downloadId = downloadManager.enqueue(request)
        registerCompletionReceiver(context, downloadId, apkFile)
        return true
    }

    private fun registerCompletionReceiver(context: Context, downloadId: Long, apkFile: File) {
        val appContext = context.applicationContext
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val completedId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (completedId == downloadId) {
                    appContext.unregisterReceiver(this)
                    installApk(appContext, apkFile)
                }
            }
        }
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            appContext.registerReceiver(receiver, filter)
        }
    }

    private fun installApk(context: Context, apkFile: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apkFile)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }
}

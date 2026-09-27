package com.turkuaz.full.data

import android.content.Context
import android.provider.Settings
import java.security.MessageDigest

/**
 * Bolum 0.2: cihaz parmak izi. MVP: ANDROID_ID SHA-256 ile hashlenip
 * kullaniliyor (TurkuazBeta ile ayni yaklasim).
 */
object DeviceId {
    fun get(context: Context): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "unknown-device"
        val digest = MessageDigest.getInstance("SHA-256").digest(androidId.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}

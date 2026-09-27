package com.turkuaz.full.data

import org.json.JSONArray
import org.json.JSONObject

/**
 * Veri modelleri + manuel JSON (de)serilestirme. TurkuazBeta'daki gibi
 * Moshi/Gson yerine org.json (Android SDK'sinin parcasi) kullanildi -
 * bagimlilik yuzeyi kucuk tutuldu.
 */

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val role: String,
) {
    companion object {
        fun fromJson(o: JSONObject) = TokenResponse(
            accessToken = o.getString("access_token"),
            refreshToken = o.getString("refresh_token"),
            role = o.getString("role"),
        )
    }
}

data class UserMemoryItem(
    val id: Int,
    val key: String,
    val value: String,
) {
    companion object {
        fun fromJson(o: JSONObject) = UserMemoryItem(
            id = o.getInt("id"), key = o.getString("key"), value = o.getString("value"),
        )
    }
}

data class ChatReply(
    val reply: String,
    val provider: String,
) {
    companion object {
        fun fromJson(o: JSONObject) = ChatReply(
            reply = o.getString("reply"),
            provider = o.optString("provider", "?"),
        )
    }
}

data class AppVersionInfo(
    val packageName: String,
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val releaseNotes: String,
    val forceUpdate: Boolean,
) {
    companion object {
        fun fromJson(o: JSONObject) = AppVersionInfo(
            packageName = o.getString("package_name"),
            versionCode = o.getInt("version_code"),
            versionName = o.getString("version_name"),
            apkUrl = o.getString("apk_url"),
            releaseNotes = o.optString("release_notes", ""),
            forceUpdate = o.optBoolean("force_update", false),
        )
    }
}

fun parseUserMemory(arr: JSONArray): List<UserMemoryItem> =
    (0 until arr.length()).map { UserMemoryItem.fromJson(arr.getJSONObject(it)) }

/** Sunucudan gelen hata govdesi: {"detail": "..."} */
fun parseErrorDetail(body: String): String =
    try {
        JSONObject(body).optString("detail", body)
    } catch (e: Exception) {
        body
    }

class ApiException(val statusCode: Int, message: String) : Exception(message)

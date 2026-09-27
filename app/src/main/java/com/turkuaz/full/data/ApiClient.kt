package com.turkuaz.full.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * TURKUAZ CORE API istemcisi. TurkuazBeta'daki gibi bilerek ciplak
 * HttpURLConnection kullanildi (Retrofit/OkHttp yok) - bagimlilik yuzeyi
 * kucuk. Is mantiginin TAMAMI sunucuda; bu sinif sadece HTTP cagrilarini
 * sarmalar.
 */
class ApiClient(private val session: SessionStore, private val deviceFingerprint: String) {

    private suspend fun request(
        method: String,
        path: String,
        body: JSONObject? = null,
        authorized: Boolean = true,
    ): String = withContext(Dispatchers.IO) {
        val url = URL(Config.BASE_URL + path)
        val conn = url.openConnection() as HttpURLConnection
        try {
            conn.requestMethod = method
            conn.connectTimeout = 15_000
            conn.readTimeout = 30_000
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("X-Device-Fingerprint", deviceFingerprint)
            if (authorized) {
                session.accessToken?.let { conn.setRequestProperty("Authorization", "Bearer $it") }
            }
            if (body != null) {
                conn.doOutput = true
                OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(body.toString()) }
            }

            val status = conn.responseCode
            val stream = if (status in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.let { s ->
                BufferedReader(InputStreamReader(s, Charsets.UTF_8)).use { it.readText() }
            } ?: ""

            if (status !in 200..299) {
                throw ApiException(status, parseErrorDetail(text))
            }
            text
        } finally {
            conn.disconnect()
        }
    }

    // ---- Auth ----

    suspend fun login(username: String, password: String): TokenResponse {
        val body = JSONObject()
            .put("username", username)
            .put("password", password)
            .put("device_fingerprint", deviceFingerprint)
        val resp = request("POST", "/api/v1/auth/login", body, authorized = false)
        val token = TokenResponse.fromJson(JSONObject(resp))
        session.accessToken = token.accessToken
        session.refreshToken = token.refreshToken
        session.role = token.role
        return token
    }

    suspend fun register(username: String, password: String): TokenResponse {
        val body = JSONObject()
            .put("username", username)
            .put("password", password)
            .put("device_fingerprint", deviceFingerprint)
        val resp = request("POST", "/api/v1/auth/register", body, authorized = false)
        val token = TokenResponse.fromJson(JSONObject(resp))
        session.accessToken = token.accessToken
        session.refreshToken = token.refreshToken
        session.role = token.role
        return token
    }

    // ---- Sohbet ----

    suspend fun sendChat(message: String): ChatReply {
        val body = JSONObject().put("message", message)
        val resp = request("POST", "/api/v1/chat", body)
        return ChatReply.fromJson(JSONObject(resp))
    }

    // ---- Kullanici Hafizasi ----

    suspend fun getUserMemory(): List<UserMemoryItem> {
        val resp = request("GET", "/api/v1/memory/me")
        return parseUserMemory(JSONArray(resp))
    }

    suspend fun deleteUserMemory(memoryId: Int) {
        request("DELETE", "/api/v1/memory/me/$memoryId")
    }

    // ---- Geri bildirim ----

    suspend fun submitFeedback(message: String, category: String) {
        val body = JSONObject().put("message", message).put("category", category)
        request("POST", "/api/v1/feedback", body)
    }

    // ---- Uygulama ici guncelleme (in-app update) ----

    /**
     * Auth gerektirmez (server tarafinda da acik) - uygulama acilisinda,
     * kullanici giris yapmadan once bile kontrol edilebilsin diye.
     * Sunucuda o paket icin kayitli bir surum yoksa 404 doner - cagiran
     * taraf (MainActivity) bunu "guncelleme yok" olarak yorumlayip
     * sessizce gecmeli.
     */
    suspend fun getAppVersion(packageName: String): AppVersionInfo {
        val resp = request("GET", "/api/v1/app/version?package_name=$packageName", authorized = false)
        return AppVersionInfo.fromJson(JSONObject(resp))
    }
}

package com.turkuaz.full.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class ChatLine(val fromUser: Boolean, val text: String) {
    fun toJson(): JSONObject = JSONObject().put("fromUser", fromUser).put("text", text)

    companion object {
        fun fromJson(o: JSONObject) = ChatLine(o.getBoolean("fromUser"), o.getString("text"))
    }
}

data class Conversation(
    val id: String,
    val title: String,
    val updatedAt: Long,
    val messages: List<ChatLine>,
) {
    fun toJson(): JSONObject {
        val arr = JSONArray()
        messages.forEach { arr.put(it.toJson()) }
        return JSONObject()
            .put("id", id)
            .put("title", title)
            .put("updatedAt", updatedAt)
            .put("messages", arr)
    }

    companion object {
        fun fromJson(o: JSONObject): Conversation {
            val arr = o.getJSONArray("messages")
            val messages = (0 until arr.length()).map { ChatLine.fromJson(arr.getJSONObject(it)) }
            return Conversation(
                id = o.getString("id"),
                title = o.getString("title"),
                updatedAt = o.getLong("updatedAt"),
                messages = messages,
            )
        }
    }
}

/**
 * Gecmis sohbetlerin CIHAZ UZERINDE (yalnizca yerel) saklanmasi.
 *
 * Sunucuda su an bir "konusma/oturum" kavrami yok - /api/v1/chat tek
 * seferlik, durumsuz calisiyor (bkz. turkuaz-core). Bu yuzden "Gecmis
 * Sohbetler" ozelligi bilerek sunucu tarafinda degil, burada, basit bir
 * SharedPreferences + JSON ile implemente edildi (ekstra kutuphane -Room
 * vb.- eklemeden). Kullanici uygulamayi silerse ya da baska bir cihaza
 * gecerse gecmisi kaybeder - bu bilinen bir sinirlama (TODO: istenirse
 * sunucu tarafinda kalici bir Conversation modeli eklenebilir).
 */
class ChatHistoryStore(context: Context) {
    private val prefs = context.getSharedPreferences("turkuaz_chat_history", Context.MODE_PRIVATE)

    fun getAll(): List<Conversation> {
        val raw = prefs.getString("conversations", null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { Conversation.fromJson(arr.getJSONObject(it)) }
                .sortedByDescending { it.updatedAt }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(conversation: Conversation) {
        val all = getAll().toMutableList()
        val idx = all.indexOfFirst { it.id == conversation.id }
        if (idx >= 0) all[idx] = conversation else all.add(conversation)

        val arr = JSONArray()
        all.forEach { arr.put(it.toJson()) }
        prefs.edit().putString("conversations", arr.toString()).apply()
    }

    fun get(id: String): Conversation? = getAll().firstOrNull { it.id == id }
}

fun makeConversationTitle(firstUserMessage: String): String =
    firstUserMessage.trim().take(40).ifBlank { "Sohbet" }

package com.gunz.chatbotg

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class TikTokResult(
    val id: String,
    val title: String,
    val author: String,
    val videoUrl: String,
    val hdUrl: String?,
    val musicUrl: String?,
    val duration: Int,
    val plays: Long,
    val likes: Long,
    val comments: Long,
    val shares: Long
)

/** Memakai API publik tikwm.com (video tanpa watermark). */
object TikTokApi {
    private const val BASE = "https://www.tikwm.com"

    private fun abs(u: String?): String? {
        if (u.isNullOrBlank()) return null
        return if (u.startsWith("http")) u else BASE + (if (u.startsWith("/")) "" else "/") + u
    }

    fun fetch(url: String): TikTokResult {
        val api = "$BASE/api/?hd=1&url=" + URLEncoder.encode(url, "UTF-8")
        val conn = URL(api).openConnection() as HttpURLConnection
        conn.connectTimeout = 15000
        conn.readTimeout = 20000
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120 Mobile Safari/537.36")
        val body = try {
            conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
        val json = JSONObject(body)
        if (json.optInt("code", -1) != 0) {
            throw Exception(json.optString("msg", "Video tidak ditemukan"))
        }
        val d = json.getJSONObject("data")
        val author = d.optJSONObject("author")
        val play = abs(d.optString("play")) ?: throw Exception("Link video tidak tersedia")
        return TikTokResult(
            id = d.optString("id", System.currentTimeMillis().toString()),
            title = d.optString("title", "").ifBlank { "(tanpa judul)" },
            author = author?.optString("nickname").orEmpty() +
                (author?.optString("unique_id")?.let { " (@$it)" } ?: ""),
            videoUrl = play,
            hdUrl = abs(d.optString("hdplay")),
            musicUrl = abs(d.optString("music")),
            duration = d.optInt("duration", 0),
            plays = d.optLong("play_count", 0),
            likes = d.optLong("digg_count", 0),
            comments = d.optLong("comment_count", 0),
            shares = d.optLong("share_count", 0)
        )
    }
}

package com.gunz.chatbotg

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Tombol di dalam balasan bot. Isi [command] ATAU [action]. */
data class Btn(val label: String, val command: String? = null, val action: (() -> Unit)? = null)

class ChatMessage(
    val fromUser: Boolean,
    val text: String = "",
    val buttons: List<Btn> = emptyList(),
    val game: TicTacToe? = null,
    val time: String = Util.now()
)

object Util {
    fun now(): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

    fun dp(ctx: Context, v: Int): Int = (v * ctx.resources.displayMetrics.density).toInt()

    private val tiktokRegex = Regex("https?://\\S*tiktok\\.com\\S*", RegexOption.IGNORE_CASE)

    fun tiktokUrl(text: String): String? = tiktokRegex.find(text)?.value

    fun fmt(n: Long): String = java.text.NumberFormat.getInstance(Locale("id", "ID")).format(n)
}

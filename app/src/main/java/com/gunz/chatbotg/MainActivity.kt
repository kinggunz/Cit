package com.gunz.chatbotg

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator

class MainActivity : AppCompatActivity(), Bot.Host {

    private val items = ArrayList<ChatMessage>()
    private lateinit var adapter: ChatAdapter
    private lateinit var rv: RecyclerView
    private lateinit var etMsg: EditText
    private lateinit var bot: Bot
    private var pendingDownload: Pair<String, String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val user = UserStore.current(this)
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        setContentView(R.layout.activity_main)

        findViewById<TextView>(R.id.tvSub).text = "${Bot.NAME} • online • $user"
        findViewById<TextView>(R.id.btnLogout).setOnClickListener { logout() }

        rv = findViewById(R.id.rv)
        rv.layoutManager = LinearLayoutManager(this)
        (rv.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        adapter = ChatAdapter(items) { cmd -> send(cmd) }
        rv.adapter = adapter

        bot = Bot(this, this, user)

        etMsg = findViewById(R.id.etMsg)
        findViewById<TextView>(R.id.btnSend).setOnClickListener { sendFromInput() }
        etMsg.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) { sendFromInput(); true } else false
        }

        // tombol cepat
        val chips = findViewById<LinearLayout>(R.id.chips)
        listOf(".bot", ".menu", ".ping", ".tiktok", ".tictactoe", ".dadu", ".quote").forEach { cmd ->
            val tv = TextView(this).apply {
                text = cmd
                setTextColor(0xFFFFFFFF.toInt())
                textSize = 13f
                setBackgroundResource(R.drawable.bg_chip)
                setPadding(Util.dp(context, 14), Util.dp(context, 8), Util.dp(context, 14), Util.dp(context, 8))
                isClickable = true
                isFocusable = true
                setOnClickListener { send(cmd) }
            }
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = Util.dp(this@MainActivity, 8) }
            chips.addView(tv, lp)
        }

        bot.welcome()
    }

    private fun sendFromInput() {
        val t = etMsg.text.toString().trim()
        if (t.isEmpty()) return
        etMsg.setText("")
        send(t)
    }

    private fun send(text: String) {
        addMessage(ChatMessage(true, text))
        bot.onUser(text)
    }

    private fun addMessage(m: ChatMessage) {
        items.add(m)
        adapter.notifyItemInserted(items.size - 1)
        rv.scrollToPosition(items.size - 1)
    }

    // ---------- Bot.Host ----------
    override fun post(msg: ChatMessage) {
        if (isFinishing || isDestroyed) return
        addMessage(msg)
    }

    override fun clear() {
        items.clear()
        adapter.notifyDataSetChanged()
        post(ChatMessage(false, "🧹 Chat dibersihkan. Ketik .menu untuk melihat fitur."))
    }

    override fun logout() {
        UserStore.logout(this)
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            Toast.makeText(this, "Tidak ada aplikasi untuk membuka link", Toast.LENGTH_SHORT).show()
        }
    }

    override fun download(url: String, filename: String) {
        if (Build.VERSION.SDK_INT <= 28 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            pendingDownload = url to filename
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 11
            )
            return
        }
        try {
            val mime = if (filename.endsWith(".mp3")) "audio/mpeg" else "video/mp4"
            val req = DownloadManager.Request(Uri.parse(url))
                .setTitle(filename)
                .setDescription("ChatBotG")
                .setMimeType(mime)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "ChatBotG/$filename")
            (getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(req)
            post(
                ChatMessage(
                    false,
                    "⬇️ Download dimulai!\nFile: $filename\nTersimpan di: Download/ChatBotG\nPantau progres di notifikasi.",
                    listOf(Btn("📋 Menu", ".menu"))
                )
            )
        } catch (e: Exception) {
            post(ChatMessage(false, "❌ Gagal memulai download: ${e.message}"))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 11) {
            val p = pendingDownload
            pendingDownload = null
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && p != null) {
                download(p.first, p.second)
            } else {
                post(ChatMessage(false, "❌ Izin penyimpanan ditolak, download dibatalkan."))
            }
        }
    }
}

package com.gunz.chatbotg

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Base64
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

/** Otak bot GunzAPK. Semua balasan dikirim lewat [Host.post] di main thread. */
class Bot(private val ctx: Context, private val host: Host, private val username: String) {

    interface Host {
        fun post(msg: ChatMessage)
        fun clear()
        fun logout()
        fun download(url: String, filename: String)
        fun openUrl(url: String)
    }

    companion object {
        const val NAME = "GunzAPK"
        const val CREATOR = "Gunz"
    }

    private val main = Handler(Looper.getMainLooper())
    private val startTime = System.currentTimeMillis()
    private var cmdCount = 0
    private var guessTarget: Int? = null
    private var guessTries = 0

    private val idLocale = Locale("id", "ID")

    // ---------- util kirim ----------
    private fun say(text: String, buttons: List<Btn> = emptyList(), delay: Long = 350) {
        main.postDelayed({ host.post(ChatMessage(false, text, buttons)) }, delay)
    }

    private fun menuBtn() = Btn("📋 Menu", ".menu")

    private fun versionName(): String = try {
        @Suppress("DEPRECATION")
        ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "1.0.0"
    } catch (e: Exception) { "1.0.0" }

    fun welcome() {
        say(
            "Halo $username! 👋\nSelamat datang di ChatBotG.\n\n" +
                "Ketik .bot untuk melihat info bot atau .menu untuk daftar fitur.",
            listOf(Btn("🤖 Info Bot", ".bot"), menuBtn()),
            delay = 400
        )
    }

    // ---------- input ----------
    fun onUser(raw: String) {
        val input = raw.trim()
        if (input.isEmpty()) return
        cmdCount++

        if (input.startsWith(".")) {
            val parts = input.split(Regex("\\s+"), limit = 2)
            val cmd = parts[0].lowercase().drop(1)
            val arg = parts.getOrElse(1) { "" }.trim()
            command(cmd, arg)
            return
        }

        // link TikTok tanpa perintah -> langsung proses
        Util.tiktokUrl(input)?.let { tiktok(it); return }

        // sedang main tebak angka
        val g = guessTarget
        val n = input.toIntOrNull()
        if (g != null && n != null) { guess(g, n); return }

        say(
            "Aku belum paham pesan itu 🤔\nPerintah diawali titik, contoh: .bot atau .menu",
            listOf(Btn("🤖 Info Bot", ".bot"), menuBtn())
        )
    }

    private fun command(cmd: String, arg: String) {
        when (cmd) {
            "bot", "info" -> botInfo()
            "menu", "help", "start" -> menu()
            "profile", "profil", "me" -> profile()
            "uptime", "runtime" -> uptime()
            "clear", "bersih" -> host.clear()
            "logout", "keluar" -> host.logout()

            "tiktok", "tt", "ttdl" -> tiktok(arg, fromCommand = true)

            "ping" -> ping()
            "waktu", "time", "tanggal", "date" -> waktu()
            "calc", "hitung", "kalkulator" -> calc(arg)
            "pw", "password" -> password(arg)
            "reverse", "balik" -> textTool("🔁 Dibalik", arg) { it.reversed() }
            "upper", "besar" -> textTool("🔠 Huruf besar", arg) { it.uppercase() }
            "lower", "kecil" -> textTool("🔡 Huruf kecil", arg) { it.lowercase() }
            "count", "hitungkata" -> count(arg)
            "base64" -> base64(arg)

            "tictactoe", "ttt", "xo" -> ticTacToe()
            "game", "games" -> gameMenu()
            "tebak" -> startGuess()
            "suit" -> suit(arg)
            "dadu", "dice" -> dadu()
            "koin", "coin" -> koin()
            "8ball", "tanya" -> eightBall(arg)

            "quote", "kata" -> quote()
            "fakta", "fact" -> fakta()
            "lelucon", "joke" -> lelucon()

            else -> say(
                "❓ Perintah .$cmd tidak dikenal.\nCoba buka menu untuk melihat semua perintah.",
                listOf(menuBtn())
            )
        }
    }

    // ---------- info & menu ----------
    private fun botInfo() {
        val now = SimpleDateFormat("EEEE, dd MMM yyyy HH:mm:ss", idLocale).format(Date())
        say(
            "🤖 INFO BOT\n" +
                "━━━━━━━━━━━━━━\n" +
                "Nama Bot   : $NAME\n" +
                "Versi      : v${versionName()}\n" +
                "Aplikasi   : ChatBotG\n" +
                "Pembuat    : $CREATOR\n" +
                "Status     : Online ✅\n" +
                "Uptime     : ${formatDuration(System.currentTimeMillis() - startTime)}\n" +
                "Pesan masuk: $cmdCount\n" +
                "Pengguna   : $username\n" +
                "Perangkat  : ${Build.MANUFACTURER} ${Build.MODEL}\n" +
                "Android    : ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})\n" +
                "Waktu      : $now\n" +
                "Total fitur: 25+ perintah\n" +
                "━━━━━━━━━━━━━━\n" +
                "Ketik .menu untuk melihat semua fitur.",
            listOf(menuBtn(), Btn("🏓 Ping", ".ping"))
        )
    }

    private fun menu() {
        say(
            "📋 MENU $NAME\n" +
                "━━━━━━━━━━━━━━\n" +
                "🤖 UTAMA\n" +
                ".bot  • info bot\n" +
                ".menu • daftar menu\n" +
                ".profile • profil akunmu\n" +
                ".uptime • lama bot aktif\n" +
                ".clear • bersihkan chat\n" +
                ".logout • keluar akun\n\n" +
                "⬇️ DOWNLOADER\n" +
                ".tiktok <link> • video TikTok tanpa watermark (+MP3)\n\n" +
                "🛠 TOOLS\n" +
                ".ping • cek kecepatan koneksi\n" +
                ".waktu • tanggal & jam\n" +
                ".calc <hitungan> • kalkulator\n" +
                ".pw <panjang> • buat password acak\n" +
                ".reverse / .upper / .lower <teks>\n" +
                ".count <teks> • hitung kata\n" +
                ".base64 enc|dec <teks>\n\n" +
                "🎮 GAME\n" +
                ".tictactoe • XO lawan bot\n" +
                ".tebak • tebak angka 1-100\n" +
                ".suit <batu|gunting|kertas>\n" +
                ".dadu • .koin • .8ball <tanya>\n\n" +
                "😄 HIBURAN\n" +
                ".quote • .fakta • .lelucon\n" +
                "━━━━━━━━━━━━━━\n" +
                "Tip: tempel link TikTok langsung di chat, bot otomatis memproses.",
            listOf(
                Btn("🎵 TikTok", ".tiktok"),
                Btn("🏓 Ping", ".ping"),
                Btn("⭕ Tic Tac Toe", ".tictactoe"),
                Btn("🎮 Semua Game", ".game"),
                Btn("🎲 Dadu", ".dadu"),
                Btn("💬 Quote", ".quote"),
                Btn("🤖 Info Bot", ".bot")
            )
        )
    }

    private fun profile() = say(
        "👤 PROFIL\nUsername : $username\nStatus   : Member ChatBotG\nPesan dikirim sesi ini: $cmdCount",
        listOf(menuBtn())
    )

    private fun uptime() = say(
        "⏱ Bot aktif selama ${formatDuration(System.currentTimeMillis() - startTime)}",
        listOf(menuBtn())
    )

    private fun formatDuration(ms: Long): String {
        val s = ms / 1000
        return "${s / 3600} jam ${(s % 3600) / 60} menit ${s % 60} detik"
    }

    // ---------- TikTok ----------
    private fun tiktok(arg: String, fromCommand: Boolean = false) {
        if (arg.isBlank()) {
            say(
                "🎵 TIKTOK DOWNLOADER (tanpa watermark)\n\nCara pakai:\n.tiktok https://vt.tiktok.com/xxxx\n\n" +
                    "Atau tempel saja link TikTok di chat.",
                listOf(menuBtn())
            )
            return
        }
        val url = Util.tiktokUrl(arg)
        if (url == null) {
            say("❌ Link TikTok tidak valid.\nContoh: .tiktok https://vt.tiktok.com/xxxx", listOf(menuBtn()))
            return
        }
        say("⏳ Mengambil video TikTok, tunggu sebentar...", delay = 200)
        Thread {
            try {
                val r = TikTokApi.fetch(url)
                val btns = mutableListOf<Btn>()
                btns += Btn("⬇️ Download Video (No Watermark)", action = {
                    host.download(r.videoUrl, "ChatBotG_tt_${r.id}.mp4")
                })
                if (r.hdUrl != null && r.hdUrl != r.videoUrl) {
                    btns += Btn("🎬 Download HD", action = {
                        host.download(r.hdUrl, "ChatBotG_tt_${r.id}_HD.mp4")
                    })
                }
                if (r.musicUrl != null) {
                    btns += Btn("🎧 Download MP3", action = {
                        host.download(r.musicUrl, "ChatBotG_tt_${r.id}.mp3")
                    })
                }
                btns += Btn("🌐 Buka di Browser", action = { host.openUrl(r.videoUrl) })
                btns += menuBtn()
                val text = "✅ Video ditemukan!\n" +
                    "━━━━━━━━━━━━━━\n" +
                    "Judul   : ${r.title.take(120)}\n" +
                    "Author  : ${r.author}\n" +
                    "Durasi  : ${r.duration} detik\n" +
                    "👁 ${Util.fmt(r.plays)}   ❤️ ${Util.fmt(r.likes)}\n" +
                    "💬 ${Util.fmt(r.comments)}   🔗 ${Util.fmt(r.shares)}\n" +
                    "━━━━━━━━━━━━━━\n" +
                    "Pilih tombol di bawah untuk mengunduh."
                main.post { host.post(ChatMessage(false, text, btns)) }
            } catch (e: Exception) {
                main.post {
                    host.post(
                        ChatMessage(
                            false,
                            "❌ Gagal mengambil video.\nAlasan: ${e.message ?: "koneksi bermasalah"}\n\n" +
                                "Pastikan link benar, video tidak private, dan internet aktif.",
                            listOf(Btn("🔁 Coba lagi", ".tiktok $url"), menuBtn())
                        )
                    )
                }
            }
        }.start()
    }

    // ---------- Ping ----------
    private fun httpPing(url: String): Long? = try {
        val t = System.nanoTime()
        val c = URL(url).openConnection() as HttpURLConnection
        c.connectTimeout = 5000
        c.readTimeout = 5000
        c.requestMethod = "GET"
        c.responseCode
        c.disconnect()
        (System.nanoTime() - t) / 1_000_000
    } catch (e: Exception) { null }

    private fun ping() {
        val t0 = System.nanoTime()
        say("🏓 Mengukur ping...", delay = 0)
        Thread {
            val google = httpPing("https://www.google.com/generate_204")
            val tikwm = httpPing("https://www.tikwm.com/")
            val cf = httpPing("https://www.cloudflare.com/cdn-cgi/trace")
            val total = (System.nanoTime() - t0) / 1_000_000
            fun show(v: Long?) = if (v == null) "timeout ❌" else "$v ms"
            val best = listOfNotNull(google, tikwm, cf).minOrNull()
            val quality = when {
                best == null -> "Tidak ada koneksi 🔴"
                best < 100 -> "Sangat cepat 🟢"
                best < 250 -> "Cepat 🟡"
                else -> "Lambat 🟠"
            }
            val text = "🏓 Pong!\n" +
                "━━━━━━━━━━━━━━\n" +
                "Google      : ${show(google)}\n" +
                "Cloudflare  : ${show(cf)}\n" +
                "TikWM (API) : ${show(tikwm)}\n" +
                "Total tes   : $total ms\n" +
                "Kualitas    : $quality"
            main.post {
                host.post(ChatMessage(false, text, listOf(Btn("🔁 Ping lagi", ".ping"), menuBtn())))
            }
        }.start()
    }

    // ---------- Tools ----------
    private fun waktu() {
        val d = Date()
        val tgl = SimpleDateFormat("EEEE, dd MMMM yyyy", idLocale).format(d)
        val jam = SimpleDateFormat("HH:mm:ss z", idLocale).format(d)
        say("🕒 WAKTU\nTanggal : $tgl\nJam     : $jam", listOf(menuBtn()))
    }

    private class Calc(src: String) {
        private val s = src.replace(" ", "").replace(",", ".")
            .replace("x", "*").replace("×", "*").replace("÷", "/")
        private var p = 0

        fun eval(): Double {
            val v = expr()
            if (p != s.length) throw IllegalArgumentException("Ekspresi tidak valid")
            return v
        }

        private fun expr(): Double {
            var v = term()
            while (p < s.length && (s[p] == '+' || s[p] == '-')) {
                val op = s[p++]
                val r = term()
                v = if (op == '+') v + r else v - r
            }
            return v
        }

        private fun term(): Double {
            var v = pow()
            while (p < s.length && (s[p] == '*' || s[p] == '/' || s[p] == '%')) {
                val op = s[p++]
                val r = pow()
                v = when (op) {
                    '*' -> v * r
                    '/' -> {
                        if (r == 0.0) throw ArithmeticException("Tidak bisa dibagi nol")
                        v / r
                    }
                    else -> v % r
                }
            }
            return v
        }

        private fun pow(): Double {
            val b = unary()
            if (p < s.length && s[p] == '^') {
                p++
                return Math.pow(b, pow())
            }
            return b
        }

        private fun unary(): Double {
            if (p < s.length && s[p] == '-') { p++; return -unary() }
            if (p < s.length && s[p] == '+') { p++; return unary() }
            return primary()
        }

        private fun primary(): Double {
            if (p < s.length && s[p] == '(') {
                p++
                val v = expr()
                if (p >= s.length || s[p] != ')') throw IllegalArgumentException("Kurung tidak seimbang")
                p++
                return v
            }
            val st = p
            while (p < s.length && (s[p].isDigit() || s[p] == '.')) p++
            if (st == p) throw IllegalArgumentException("Ekspresi tidak valid")
            return s.substring(st, p).toDouble()
        }
    }

    private fun calc(arg: String) {
        if (arg.isBlank()) {
            say("🧮 Contoh: .calc 12*(3+4)/2\nOperator: + - * / % ^ dan kurung ( )", listOf(menuBtn()))
            return
        }
        try {
            val res = Calc(arg).eval()
            if (res.isNaN() || res.isInfinite()) throw ArithmeticException("Hasil tidak valid")
            val txt = if (res % 1.0 == 0.0 && Math.abs(res) < 1e15) res.toLong().toString()
            else String.format(Locale.US, "%.6f", res).trimEnd('0').trimEnd('.')
            say("🧮 KALKULATOR\n$arg\n= $txt", listOf(menuBtn()))
        } catch (e: Exception) {
            say("❌ ${e.message ?: "Ekspresi tidak valid"}", listOf(menuBtn()))
        }
    }

    private fun password(arg: String) {
        val len = (arg.toIntOrNull() ?: 12).coerceIn(8, 64)
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$%&*?"
        val rnd = SecureRandom()
        val pw = (1..len).map { chars[rnd.nextInt(chars.length)] }.joinToString("")
        say("🔐 PASSWORD ACAK ($len karakter)\n\n$pw\n\n(Tekan lama teks untuk menyalin)",
            listOf(Btn("🔁 Buat lagi", ".pw $len"), menuBtn()))
    }

    private fun textTool(title: String, arg: String, f: (String) -> String) {
        if (arg.isBlank()) { say("Tulis teksnya setelah perintah.\nContoh: .upper halo dunia", listOf(menuBtn())); return }
        say("$title\n${f(arg)}", listOf(menuBtn()))
    }

    private fun count(arg: String) {
        if (arg.isBlank()) { say("Contoh: .count aku suka makan nasi", listOf(menuBtn())); return }
        val words = arg.trim().split(Regex("\\s+")).size
        say("🔢 HITUNG TEKS\nKata      : $words\nKarakter  : ${arg.length}\nTanpa spasi: ${arg.count { !it.isWhitespace() }}",
            listOf(menuBtn()))
    }

    private fun base64(arg: String) {
        val parts = arg.split(Regex("\\s+"), limit = 2)
        if (parts.size < 2 || parts[0].lowercase() !in listOf("enc", "dec")) {
            say("Contoh:\n.base64 enc halo\n.base64 dec aGFsbw==", listOf(menuBtn()))
            return
        }
        try {
            val out = if (parts[0].lowercase() == "enc")
                Base64.encodeToString(parts[1].toByteArray(), Base64.NO_WRAP)
            else String(Base64.decode(parts[1], Base64.DEFAULT))
            say("🔐 BASE64\n$out", listOf(menuBtn()))
        } catch (e: Exception) {
            say("❌ Input Base64 tidak valid", listOf(menuBtn()))
        }
    }

    // ---------- Game ----------
    private fun gameMenu() = say(
        "🎮 DAFTAR GAME\n\n⭕ Tic Tac Toe – lawan bot, kolom bisa dipencet\n🔢 Tebak Angka – 1 sampai 100\n" +
            "✊ Suit – batu gunting kertas\n🎲 Dadu\n🪙 Koin",
        listOf(
            Btn("⭕ Tic Tac Toe", ".tictactoe"),
            Btn("🔢 Tebak Angka", ".tebak"),
            Btn("✊ Suit", ".suit"),
            Btn("🎲 Dadu", ".dadu"),
            Btn("🪙 Koin", ".koin")
        )
    )

    private fun ticTacToe() {
        main.postDelayed({
            host.post(ChatMessage(false, "", game = TicTacToe()))
        }, 300)
    }

    private fun startGuess() {
        guessTarget = Random.nextInt(1, 101)
        guessTries = 0
        say("🔢 TEBAK ANGKA\nAku memikirkan angka 1-100.\nKamu punya 7 kesempatan. Ketik angkanya di chat!")
    }

    private fun guess(target: Int, n: Int) {
        guessTries++
        when {
            n == target -> {
                guessTarget = null
                say("🎉 Benar! Angkanya $target.\nKamu menebak dalam $guessTries percobaan.",
                    listOf(Btn("🔁 Main lagi", ".tebak"), menuBtn()))
            }
            guessTries >= 7 -> {
                guessTarget = null
                say("😅 Kesempatan habis! Angkanya adalah $target.",
                    listOf(Btn("🔁 Main lagi", ".tebak"), menuBtn()))
            }
            n < target -> say("⬆️ Terlalu kecil! Sisa kesempatan: ${7 - guessTries}")
            else -> say("⬇️ Terlalu besar! Sisa kesempatan: ${7 - guessTries}")
        }
    }

    private fun suit(arg: String) {
        val opts = listOf("batu", "gunting", "kertas")
        val me = arg.lowercase()
        if (me !in opts) {
            say("✊✌️✋ SUIT\nPilih senjatamu:", listOf(
                Btn("✊ Batu", ".suit batu"), Btn("✌️ Gunting", ".suit gunting"), Btn("✋ Kertas", ".suit kertas")
            ))
            return
        }
        val bot = opts.random()
        val result = when {
            me == bot -> "Seri 🤝"
            (me == "batu" && bot == "gunting") || (me == "gunting" && bot == "kertas") || (me == "kertas" && bot == "batu") -> "Kamu menang 🎉"
            else -> "Kamu kalah 😆"
        }
        say("Kamu : $me\nBot  : $bot\n\n$result", listOf(Btn("🔁 Main lagi", ".suit"), menuBtn()))
    }

    private fun dadu() = say("🎲 Dadu berhenti di angka ${Random.nextInt(1, 7)}!",
        listOf(Btn("🎲 Lempar lagi", ".dadu"), menuBtn()))

    private fun koin() = say("🪙 Hasilnya: ${if (Random.nextBoolean()) "ANGKA" else "GAMBAR"}!",
        listOf(Btn("🪙 Lempar lagi", ".koin"), menuBtn()))

    private fun eightBall(arg: String) {
        if (arg.isBlank()) { say("🎱 Tanyakan sesuatu.\nContoh: .8ball apakah hari ini hoki?", listOf(menuBtn())); return }
        val a = listOf(
            "Pasti iya ✅", "Kemungkinan besar iya 👍", "Tanya lagi nanti 🤔", "Jangan terlalu berharap 😬",
            "Sepertinya tidak ❌", "Tanda-tandanya bagus ✨", "Aku ragu 🫤", "Tentu saja! 🎉"
        )
        say("🎱 $arg\n\nJawab: ${a.random()}", listOf(menuBtn()))
    }

    // ---------- Hiburan ----------
    private fun quote() {
        val q = listOf(
            "Mulai saja dulu, sempurna belakangan.",
            "Kesabaran adalah kunci dari semua pintu yang terkunci.",
            "Gagal itu biasa, menyerah itu pilihan.",
            "Satu langkah kecil setiap hari lebih baik daripada diam.",
            "Kerja keras mengalahkan bakat saat bakat tidak bekerja keras.",
            "Hari ini adalah kesempatan baru untuk jadi lebih baik."
        )
        say("💬 \"${q.random()}\"", listOf(Btn("🔁 Lagi", ".quote"), menuBtn()))
    }

    private fun fakta() {
        val f = listOf(
            "Madu hampir tidak pernah basi.",
            "Gurita punya tiga jantung.",
            "Indonesia memiliki lebih dari 17.000 pulau.",
            "Pisang secara botani termasuk buah beri.",
            "Jantung udang berada di kepalanya.",
            "Sidik lidah setiap orang itu unik."
        )
        say("📚 Tahukah kamu?\n${f.random()}", listOf(Btn("🔁 Lagi", ".fakta"), menuBtn()))
    }

    private fun lelucon() {
        val j = listOf(
            "Kenapa komputer tidak pernah lapar? Karena sudah banyak byte-nya 😄",
            "Apa bedanya kamu dengan Wi-Fi? Wi-Fi bisa nyambung, kamu enggak 😭",
            "Kenapa programmer suka gelap? Karena light attracts bugs 🐛",
            "Ikan apa yang bisa nyanyi? Ikan paus... eh, ikan Pause ⏸️"
        )
        say("😂 ${j.random()}", listOf(Btn("🔁 Lagi", ".lelucon"), menuBtn()))
    }
}

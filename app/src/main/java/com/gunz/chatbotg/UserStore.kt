package com.gunz.chatbotg

import android.content.Context
import java.security.MessageDigest
import java.security.SecureRandom

/** Penyimpanan akun lokal (di HP). Password disimpan sebagai hash SHA-256 + salt. */
object UserStore {
    private fun prefs(c: Context) =
        c.getSharedPreferences("chatbotg_users", Context.MODE_PRIVATE)

    private fun hash(salt: String, pass: String): String =
        MessageDigest.getInstance("SHA-256").digest((salt + pass).toByteArray())
            .joinToString("") { "%02x".format(it) }

    /** Return null jika sukses, atau pesan error. */
    fun register(c: Context, username: String, pass: String, confirm: String): String? {
        val name = username.trim()
        if (!Regex("^[A-Za-z0-9_]{3,20}$").matches(name))
            return "Username 3-20 karakter (huruf, angka, underscore)"
        if (pass.length < 6) return "Password minimal 6 karakter"
        if (pass != confirm) return "Konfirmasi password tidak cocok"
        val key = "user_" + name.lowercase()
        val p = prefs(c)
        if (p.contains(key)) return "Username sudah dipakai"
        val saltBytes = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val salt = saltBytes.joinToString("") { "%02x".format(it) }
        p.edit()
            .putString(key, salt + ":" + hash(salt, pass))
            .putString("display_$key", name)
            .apply()
        return null
    }

    /** Return null jika sukses, atau pesan error. */
    fun login(c: Context, username: String, pass: String): String? {
        val name = username.trim()
        if (name.isEmpty() || pass.isEmpty()) return "Isi username dan password"
        val key = "user_" + name.lowercase()
        val p = prefs(c)
        val saved = p.getString(key, null) ?: return "Akun tidak ditemukan"
        val parts = saved.split(":")
        if (parts.size != 2 || hash(parts[0], pass) != parts[1]) return "Password salah"
        p.edit().putString("session", p.getString("display_$key", name)).apply()
        return null
    }

    fun current(c: Context): String? = prefs(c).getString("session", null)

    fun logout(c: Context) {
        prefs(c).edit().remove("session").apply()
    }
}

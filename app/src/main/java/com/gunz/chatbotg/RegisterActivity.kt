package com.gunz.chatbotg

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etUser = findViewById<EditText>(R.id.etUser)
        val etPass = findViewById<EditText>(R.id.etPass)
        val etConfirm = findViewById<EditText>(R.id.etConfirm)

        findViewById<TextView>(R.id.btnRegister).setOnClickListener {
            val u = etUser.text.toString().trim()
            val p = etPass.text.toString()
            val c = etConfirm.text.toString()

            // validasi dulu sebelum dialog konfirmasi
            when {
                !Regex("^[A-Za-z0-9_]{3,20}$").matches(u) ->
                    toast("Username 3-20 karakter (huruf, angka, underscore)")
                p.length < 6 -> toast("Password minimal 6 karakter")
                p != c -> toast("Konfirmasi password tidak cocok")
                else -> AlertDialog.Builder(this)
                    .setTitle("Konfirmasi")
                    .setMessage("Buat akun dengan username \"$u\"?")
                    .setNegativeButton("Batal", null)
                    .setPositiveButton("Ya, buat akun") { _, _ ->
                        val err = UserStore.register(this, u, p, c)
                        if (err == null) {
                            toast("Akun berhasil dibuat! Silakan masuk.")
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        } else toast(err)
                    }
                    .show()
            }
        }
        findViewById<TextView>(R.id.tvToLogin).setOnClickListener { finish() }
    }

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}

package com.gunz.chatbotg

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

/** Splash: logo CG di tengah selama 5 detik. */
class SplashActivity : AppCompatActivity() {
    private val handler = Handler(Looper.getMainLooper())

    private val next = Runnable {
        val target = if (UserStore.current(this) != null) MainActivity::class.java else LoginActivity::class.java
        startActivity(Intent(this, target))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        findViewById<ImageView>(R.id.logo).apply {
            alpha = 0f
            animate().alpha(1f).setDuration(900).start()
        }
        handler.postDelayed(next, 5000)
    }

    override fun onDestroy() {
        handler.removeCallbacks(next)
        super.onDestroy()
    }
}

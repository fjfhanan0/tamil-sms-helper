package com.tamil.smshelper.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.tamil.smshelper.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val videoView = findViewById<VideoView>(R.id.splash_video)
        videoView.setVideoURI(Uri.parse("android.resource://$packageName/${R.raw.splash}"))
        videoView.setOnCompletionListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        videoView.start()
    }
}

package com.tamil.smshelper.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.net.Uri
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.VideoView
import com.tamil.smshelper.R
import com.tamil.smshelper.util.FloatingOnTouchListener

class FloatingBallService : Service() {
    private lateinit var windowManager: WindowManager
    private var floatView: View? = null
    private var animView: VideoView? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        registerReceiver(successReceiver, IntentFilter("com.tamil.smshelper.SMS_SENT_SUCCESS"))
    }

    private val successReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            playSuccessAnimation()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showFloatView()
        return START_STICKY
    }

    private fun showFloatView() {
        floatView = LayoutInflater.from(this).inflate(R.layout.layout_floating_ball, null)
        animView = floatView?.findViewById(R.id.ball_animation)
        animView?.setVideoURI(Uri.parse("android.resource://$packageName/${R.raw.ball_loop}"))
        animView?.start()

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 100
        params.y = 200
        windowManager.addView(floatView, params)
        floatView?.setOnTouchListener(FloatingOnTouchListener(params, windowManager))
    }

    private fun playSuccessAnimation() {
        animView?.setVideoURI(Uri.parse("android.resource://$packageName/${R.raw.ball_success}"))
        animView?.start()
        animView?.setOnCompletionListener {
            animView?.setVideoURI(Uri.parse("android.resource://$packageName/${R.raw.ball_loop}"))
            animView?.start()
        }
    }

    override fun onDestroy() {
        unregisterReceiver(successReceiver)
        floatView?.let { windowManager.removeView(it) }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

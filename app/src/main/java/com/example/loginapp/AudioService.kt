package com.example.loginapp

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder

class AudioService : Service() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mediaPlayer = MediaPlayer.create(this, R.raw.sample_audio)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
        return START_STICKY
    }

    override fun onDestroy() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

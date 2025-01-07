package com.maximintegrated.maximsensorsapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.maximintegrated.maximsensorsapp.R
import timber.log.Timber
import android.app.PendingIntent
import com.maximintegrated.maximsensorsapp.MainActivity

class ForegroundService : Service() {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "MaximWellnessSuite"
        const val NOTIFICATION_ID = 1
        const val NOTIFICATION_MESSAGE_KEY = "message"
    }

    override fun onBind(intent: Intent?): IBinder? {
        Timber.d("Foreground Service onBind")
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("Foreground Service onCreate")
        createNotificationChannel()
    }

    override fun onDestroy() {
        Timber.d("Foreground Service onDestroy")
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val text = intent?.getStringExtra(NOTIFICATION_MESSAGE_KEY) ?: getString(R.string.monitoring_measurement)
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(this, System.currentTimeMillis().toInt(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_maxim_profile)
            .setSound(null)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NOTIFICATION_ID, notification)
        } else {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(NOTIFICATION_ID, notification)
        }
        return START_NOT_STICKY
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Maxim Wellness Suite",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}
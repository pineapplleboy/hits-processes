package com.example.googleclass.feature.taskdetail.service

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.googleclass.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FileUploadService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val uris = extractFileUris(intent)

        if (uris.isNullOrEmpty()) {
            stopSelf()
            return START_NOT_STICKY
        }

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildProgressNotification(0, 0, uris.size),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            else 0,
        )

        scope.launch { uploadFiles(uris) }

        return START_NOT_STICKY
    }

    private fun extractFileUris(intent: Intent?): List<Uri>? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableArrayListExtra(EXTRA_FILE_URIS, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableArrayListExtra(EXTRA_FILE_URIS)
        }
    }

    // TODO: заменить симуляцию на реальный запрос к серверу
    private suspend fun uploadFiles(uris: List<Uri>) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        uris.forEachIndexed { index, _ ->
            for (progress in 0..100 step 5) {
                delay(100)
                manager.notify(
                    NOTIFICATION_ID,
                    buildProgressNotification(progress, index, uris.size),
                )
            }
        }

        manager.notify(NOTIFICATION_ID, buildCompleteNotification())
        stopSelf()
    }

    private fun buildProgressNotification(
        progress: Int,
        currentFileIndex: Int,
        totalFiles: Int,
    ): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setContentTitle(getString(R.string.upload_notification_title))
            .setContentText(
                getString(R.string.upload_notification_progress, currentFileIndex + 1, totalFiles),
            )
            .setProgress(100, progress, false)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun buildCompleteNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .setContentTitle(getString(R.string.upload_notification_complete_title))
            .setContentText(getString(R.string.upload_notification_complete_text))
            .setAutoCancel(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    companion object {
        const val CHANNEL_ID = "file_upload_channel"
        const val NOTIFICATION_ID = 1001
        const val EXTRA_FILE_URIS = "extra_file_uris"
    }
}

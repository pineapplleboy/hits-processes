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
import com.example.googleclass.feature.taskdetail.domain.repository.FileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class FileTransferService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val fileRepository: FileRepository by inject()

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_UPLOAD -> {
                val uris = extractFileUris(intent)
                if (uris.isNullOrEmpty()) {
                    stopSelf()
                    return START_NOT_STICKY
                }
                startForegroundWithNotification(
                    UPLOAD_NOTIFICATION_ID,
                    buildUploadProgressNotification(0, 0, uris.size),
                )
                scope.launch { performUpload(uris) }
            }

            ACTION_DOWNLOAD -> {
                val fileId = intent.getStringExtra(EXTRA_FILE_ID)
                if (fileId.isNullOrEmpty()) {
                    stopSelf()
                    return START_NOT_STICKY
                }
                startForegroundWithNotification(
                    DOWNLOAD_NOTIFICATION_ID,
                    buildDownloadProgressNotification(0),
                )
                scope.launch { performDownload(fileId) }
            }

            else -> {
                stopSelf()
                return START_NOT_STICKY
            }
        }

        return START_NOT_STICKY
    }

    private suspend fun performUpload(uris: List<Uri>) {
        var lastNotifyTime = 0L

        uris.forEachIndexed { index, uri ->
            notificationManager.notify(
                UPLOAD_NOTIFICATION_ID,
                buildUploadProgressNotification(0, index, uris.size),
            )

            val result = fileRepository.uploadFile(
                uri = uri,
                contentResolver = contentResolver,
                onProgress = { percent ->
                    val now = System.currentTimeMillis()
                    if (now - lastNotifyTime >= NOTIFY_THROTTLE_MS) {
                        lastNotifyTime = now
                        notificationManager.notify(
                            UPLOAD_NOTIFICATION_ID,
                            buildUploadProgressNotification(percent, index, uris.size),
                        )
                    }
                },
            )

            if (result.isFailure) {
                notificationManager.notify(
                    UPLOAD_NOTIFICATION_ID,
                    buildUploadErrorNotification(),
                )
                stopSelf()
                return
            }
        }

        notificationManager.notify(UPLOAD_NOTIFICATION_ID, buildUploadCompleteNotification())
        stopSelf()
    }

    private suspend fun performDownload(fileId: String) {
        var lastNotifyTime = 0L

        val result = fileRepository.downloadFile(
            fileId = fileId,
            destinationDir = cacheDir,
            onProgress = { percent ->
                val now = System.currentTimeMillis()
                if (now - lastNotifyTime >= NOTIFY_THROTTLE_MS) {
                    lastNotifyTime = now
                    notificationManager.notify(
                        DOWNLOAD_NOTIFICATION_ID,
                        buildDownloadProgressNotification(percent),
                    )
                }
            },
        )

        if (result.isSuccess) {
            notificationManager.notify(
                DOWNLOAD_NOTIFICATION_ID,
                buildDownloadCompleteNotification(),
            )
        } else {
            notificationManager.notify(
                DOWNLOAD_NOTIFICATION_ID,
                buildDownloadErrorNotification(),
            )
        }
        stopSelf()
    }

    private fun startForegroundWithNotification(notificationId: Int, notification: Notification) {
        ServiceCompat.startForeground(
            this,
            notificationId,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            else 0,
        )
    }

    private fun extractFileUris(intent: Intent?): List<Uri>? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableArrayListExtra(EXTRA_FILE_URIS, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableArrayListExtra(EXTRA_FILE_URIS)
        }
    }

    private fun buildUploadProgressNotification(
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

    private fun buildUploadCompleteNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .setContentTitle(getString(R.string.upload_notification_complete_title))
            .setContentText(getString(R.string.upload_notification_complete_text))
            .setAutoCancel(true)
            .build()
    }

    private fun buildUploadErrorNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle(getString(R.string.upload_notification_error_title))
            .setContentText(getString(R.string.upload_notification_error_text))
            .setAutoCancel(true)
            .build()
    }

    private fun buildDownloadProgressNotification(progress: Int): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(getString(R.string.download_notification_title))
            .setContentText(getString(R.string.download_notification_progress))
            .setProgress(100, progress, false)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun buildDownloadCompleteNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(getString(R.string.download_notification_complete_title))
            .setContentText(getString(R.string.download_notification_complete_text))
            .setAutoCancel(true)
            .build()
    }

    private fun buildDownloadErrorNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle(getString(R.string.download_notification_error_title))
            .setContentText(getString(R.string.download_notification_error_text))
            .setAutoCancel(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    companion object {
        const val CHANNEL_ID = "file_upload_channel"
        const val UPLOAD_NOTIFICATION_ID = 1001
        const val DOWNLOAD_NOTIFICATION_ID = 1002
        const val EXTRA_FILE_URIS = "extra_file_uris"
        const val EXTRA_FILE_ID = "extra_file_id"
        const val ACTION_UPLOAD = "com.example.googleclass.ACTION_UPLOAD"
        const val ACTION_DOWNLOAD = "com.example.googleclass.ACTION_DOWNLOAD"
        private const val NOTIFY_THROTTLE_MS = 500L
    }
}

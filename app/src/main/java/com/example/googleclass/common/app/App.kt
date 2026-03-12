package com.example.googleclass.common.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.googleclass.R
import com.example.googleclass.feature.authorization.authorizationModule
import com.example.googleclass.common.network.networkModule
import com.example.googleclass.feature.course.courseDetailModule
import com.example.googleclass.feature.courses.coursesModule
import com.example.googleclass.feature.post.postModule
import com.example.googleclass.feature.profile.profileModule
import com.example.googleclass.feature.taskdetail.studentchat.studentChatModule
import com.example.googleclass.feature.taskdetail.service.FileTransferService
import com.example.googleclass.feature.taskdetail.taskDetailModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        createNotificationChannels()

        startKoin {
            androidContext(this@App)
            modules(
                networkModule,
                authorizationModule,
                taskDetailModule,
                studentChatModule,
                coursesModule,
                courseDetailModule,
                postModule,
                profileModule,
            )
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                FileTransferService.CHANNEL_ID,
                getString(R.string.upload_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = getString(R.string.upload_channel_description)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
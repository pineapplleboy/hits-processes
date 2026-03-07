package com.example.googleclass.common.app

import android.app.Application
import com.example.googleclass.feature.authorization.authorizationModule
import com.example.googleclass.feature.authorization.data.network.networkModule
import com.example.googleclass.feature.courses.coursesModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(
                networkModule,
                authorizationModule,
                coursesModule,
            )
        }
    }
}
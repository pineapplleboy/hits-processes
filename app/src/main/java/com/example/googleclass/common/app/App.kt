package com.example.googleclass.common.app

import android.app.Application
import com.example.googleclass.feature.authorization.authorizationModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(
                authorizationModule,
            )
        }
    }
}
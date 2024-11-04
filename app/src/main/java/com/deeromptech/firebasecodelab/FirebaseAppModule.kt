package com.deeromptech.firebasecodelab

import android.app.Application
import com.deeromptech.firebasecodelab.di.presentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class FirebaseAppModule : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@FirebaseAppModule)
            modules(presentationModule)
        }
    }
}
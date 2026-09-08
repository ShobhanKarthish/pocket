package com.shobhankarthish.pocket

import android.app.Application

class PocketApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

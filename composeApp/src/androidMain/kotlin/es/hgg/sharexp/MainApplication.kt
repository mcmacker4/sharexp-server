package es.hgg.sharexp

import android.app.Application

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin()
    }

}
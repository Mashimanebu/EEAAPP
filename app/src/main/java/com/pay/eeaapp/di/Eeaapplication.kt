package com.pay.eeaapp.di

import android.app.Application

class EEAApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
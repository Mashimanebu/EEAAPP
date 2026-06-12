package com.pay.eeaapp.di

import android.app.Application
import com.google.firebase.FirebaseApp

class EEAApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
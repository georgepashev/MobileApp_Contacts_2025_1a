package com.example.contacts
import android.app.Application
import com.google.firebase.FirebaseApp

class App : Application() {
    override fun onCreate() {
        super.onCreate()
// Initialize Firebase once for the process
        FirebaseApp.initializeApp(this)
// Schedule periodic background sync (15 min window, network required)
        schedulePeriodicSync(this)
// Optional: also trigger a one‐time sync at first launch
        enqueueOneTimeSync(this)
    }
}
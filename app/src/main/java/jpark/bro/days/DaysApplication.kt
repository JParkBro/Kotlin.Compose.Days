package jpark.bro.days

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DaysApplication: Application() {
    companion object {
        const val TAG = "DAYS_APPLICATION"

        fun logSomething(message: String) {
            Log.d(TAG, message)
        }
    }

    override fun onCreate() {
        super.onCreate()
        logSomething("Application On Create")
    }
}
package com.example.banksampah.data

// CloudinaryManager.kt
import android.app.Application
import android.content.Context
import com.cloudinary.android.MediaManager
import com.example.banksampah.BuildConfig

object CloudinaryManager {
    fun init(context: Context) {
        val config = mapOf(
            "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME,
            "api_key" to BuildConfig.CLOUDINARY_API_KEY,
            "api_secret" to BuildConfig.CLOUDINARY_API_SECRET
        )
        MediaManager.init(context, config)
    }
}

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CloudinaryManager.init(this)
    }
}
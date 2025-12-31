package com.example.banksampah.data

// CloudinaryManager.kt
import android.app.Application
import android.content.Context
import com.cloudinary.android.MediaManager

object CloudinaryManager {
    fun init(context: Context) {
        val config = mapOf(
            "cloud_name" to "de1lnrcww",
            "api_key" to "655736571337241",
            "api_secret" to "4BjyogQERQ-9TiEeYwj8PgqROdc"
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
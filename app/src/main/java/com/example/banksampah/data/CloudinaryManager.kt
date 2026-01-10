package com.example.banksampah.data

import android.app.Application
import android.content.Context
import com.cloudinary.android.MediaManager
import com.example.banksampah.BuildConfig

object CloudinaryManager {
    fun init(context: Context) {
        // Baca dari BuildConfig yang sudah di-inject dari secrets.properties
        val config = mapOf(
            "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME,
            "api_key" to BuildConfig.CLOUDINARY_API_KEY,
            "api_secret" to BuildConfig.CLOUDINARY_API_SECRET
        )

        // Validasi bahwa config tidak kosong
        if (config["cloud_name"].isNullOrEmpty() ||
            config["api_key"].isNullOrEmpty() ||
            config["api_secret"].isNullOrEmpty()) {
            throw IllegalStateException(
                "Cloudinary configuration is missing! " +
                        "Make sure secrets.properties file exists in project root."
            )
        }

        MediaManager.init(context, config)
    }
}

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            CloudinaryManager.init(this)
        } catch (e: Exception) {
            // Log error tapi jangan crash app
            android.util.Log.e("CloudinaryManager", "Failed to initialize Cloudinary", e)
        }
    }
}
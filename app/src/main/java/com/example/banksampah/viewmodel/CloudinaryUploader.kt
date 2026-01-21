package com.example.banksampah.viewmodel

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback


// CloudinaryUploader.kt
fun uploadToCloudinary(uri: Uri, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
    MediaManager.get().upload(uri)
        .option("resource_type", "image")
        .callback(object : UploadCallback {
            override fun onStart(requestId: String) {
                // Upload started
            }

            override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                // Progress update
            }

            // Di fungsi uploadToCloudinary, pastikan menggunakan secure_url
            override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                val secureUrl = resultData["secure_url"] as? String
                val url = resultData["url"] as? String

                // SELALU gunakan secure_url (HTTPS)
                val imageUrl = secureUrl ?: url?.replace("http://", "https://")

                if (imageUrl != null) {
                    println("DEBUG - Upload success (HTTPS): $imageUrl")
                    onSuccess(imageUrl)
                } else {
                    println("DEBUG - No URL found in response: $resultData")
                    onError("URL not found in response")
                }
            }

            override fun onError(requestId: String, error: ErrorInfo) {
                onError(error.description ?: "Unknown error")
            }

            override fun onReschedule(requestId: String, error: ErrorInfo) {
                onError(error.description ?: "Upload rescheduled")
            }
        })
        .dispatch()
}
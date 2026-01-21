package com.example.banksampah.repository

import android.net.Uri
import com.example.banksampah.data.EdukasiItem
import com.example.banksampah.data.GalleryItem
import com.example.banksampah.viewmodel.uploadToCloudinary
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class EdukasiRepository {
    private val database = FirebaseDatabase.getInstance().reference

    // Get all edukasi
    suspend fun getAllEdukasi(): Result<List<EdukasiItem>> = suspendCoroutine { continuation ->
        database.child("edukasi")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val edukasiList = mutableListOf<EdukasiItem>()
                    for (edukasiSnapshot in snapshot.children) {
                        val edukasi = edukasiSnapshot.getValue(EdukasiItem::class.java)
                        edukasi?.let {
                            if (it.id.isEmpty()) {
                                it.id = edukasiSnapshot.key ?: ""
                            }
                            edukasiList.add(it)
                        }
                    }
                    // Sort by order
                    continuation.resume(Result.success(edukasiList.sortedBy { it.order }))
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resume(Result.failure(Exception(error.message)))
                }
            })
    }

    // Get edukasi by ID
    suspend fun getEdukasiById(id: String): Result<EdukasiItem> = suspendCoroutine { continuation ->
        database.child("edukasi").child(id)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val edukasi = snapshot.getValue(EdukasiItem::class.java)
                    if (edukasi != null) {
                        if (edukasi.id.isEmpty()) {
                            edukasi.id = snapshot.key ?: id
                        }
                        continuation.resume(Result.success(edukasi))
                    } else {
                        continuation.resume(Result.failure(Exception("Edukasi tidak ditemukan")))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resume(Result.failure(Exception(error.message)))
                }
            })
    }

    // Upload image to Cloudinary
    suspend fun uploadImage(imageUri: Uri): Result<String> = suspendCoroutine { continuation ->
        uploadToCloudinary(
            uri = imageUri,
            onSuccess = { url ->
                continuation.resume(Result.success(url))
            },
            onError = { error ->
                continuation.resume(Result.failure(Exception(error)))
            }
        )
    }

    // Add new edukasi
    suspend fun addEdukasi(edukasi: EdukasiItem): Result<Unit> {
        return try {
            val newRef = database.child("edukasi").push()
            val newEdukasi = edukasi.copy(id = newRef.key ?: "")
            newRef.setValue(newEdukasi).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update edukasi
    suspend fun updateEdukasi(edukasi: EdukasiItem): Result<Unit> {
        return try {
            database.child("edukasi").child(edukasi.id)
                .setValue(edukasi).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Delete edukasi
    suspend fun deleteEdukasi(id: String): Result<Unit> {
        return try {
            database.child("edukasi").child(id).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== GALLERY FUNCTIONS ==========

    // Get all gallery items
    suspend fun getAllGallery(): Result<List<GalleryItem>> = suspendCoroutine { continuation ->
        database.child("gallery")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val galleryList = mutableListOf<GalleryItem>()
                    for (gallerySnapshot in snapshot.children) {
                        val gallery = gallerySnapshot.getValue(GalleryItem::class.java)
                        gallery?.let {
                            if (it.id.isEmpty()) {
                                it.id = gallerySnapshot.key ?: ""
                            }
                            galleryList.add(it)
                        }
                    }
                    // Sort by timestamp descending
                    continuation.resume(Result.success(galleryList.sortedByDescending { it.timestamp }))
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resume(Result.failure(Exception(error.message)))
                }
            })
    }

    // Add gallery item
    suspend fun addGalleryItem(gallery: GalleryItem): Result<Unit> {
        return try {
            val newRef = database.child("gallery").push()
            val newGallery = gallery.copy(id = newRef.key ?: "")
            newRef.setValue(newGallery).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Delete gallery item
    suspend fun deleteGalleryItem(id: String): Result<Unit> {
        return try {
            database.child("gallery").child(id).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
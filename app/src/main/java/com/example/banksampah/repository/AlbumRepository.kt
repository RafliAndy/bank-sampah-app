package com.example.banksampah.repository

import android.net.Uri
import com.example.banksampah.data.ActivityAlbum
import com.example.banksampah.data.AlbumPhoto
import com.example.banksampah.model.uploadToCloudinary
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AlbumRepository {
    private val database = FirebaseDatabase.getInstance().reference

    // ========== ALBUM OPERATIONS ==========

    // Get all albums
    suspend fun getAllAlbums(): Result<List<ActivityAlbum>> = suspendCoroutine { continuation ->
        database.child("albums")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val albumList = mutableListOf<ActivityAlbum>()
                    for (albumSnapshot in snapshot.children) {
                        val album = albumSnapshot.getValue(ActivityAlbum::class.java)
                        album?.let {
                            if (it.id.isEmpty()) {
                                it.id = albumSnapshot.key ?: ""
                            }
                            albumList.add(it)
                        }
                    }
                    // Sort by timestamp descending (newest first)
                    continuation.resume(Result.success(albumList.sortedByDescending { it.timestamp }))
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resume(Result.failure(Exception(error.message)))
                }
            })
    }

    // Get album by ID
    suspend fun getAlbumById(albumId: String): Result<ActivityAlbum> = suspendCoroutine { continuation ->
        database.child("albums").child(albumId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val album = snapshot.getValue(ActivityAlbum::class.java)
                    if (album != null) {
                        if (album.id.isEmpty()) {
                            album.id = snapshot.key ?: albumId
                        }
                        continuation.resume(Result.success(album))
                    } else {
                        continuation.resume(Result.failure(Exception("Album tidak ditemukan")))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resume(Result.failure(Exception(error.message)))
                }
            })
    }

    // Add new album
    suspend fun addAlbum(album: ActivityAlbum): Result<String> {
        return try {
            val newRef = database.child("albums").push()
            val albumId = newRef.key ?: ""
            val newAlbum = album.copy(id = albumId)
            newRef.setValue(newAlbum).await()
            Result.success(albumId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update album
    suspend fun updateAlbum(album: ActivityAlbum): Result<Unit> {
        return try {
            database.child("albums").child(album.id)
                .setValue(album).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Delete album (also deletes all photos in the album)
    suspend fun deleteAlbum(albumId: String): Result<Unit> {
        return try {
            // Delete all photos in the album first
            database.child("album_photos").orderByChild("albumId").equalTo(albumId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (photoSnapshot in snapshot.children) {
                            photoSnapshot.ref.removeValue()
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })

            // Then delete the album
            database.child("albums").child(albumId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update photo count in album
    suspend fun updateAlbumPhotoCount(albumId: String): Result<Unit> {
        return try {
            val photosRef = database.child("album_photos")
            photosRef.orderByChild("albumId").equalTo(albumId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val count = snapshot.childrenCount.toInt()
                        database.child("albums").child(albumId).child("photoCount")
                            .setValue(count)
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== PHOTO OPERATIONS ==========

    // Get photos for specific album
    suspend fun getAlbumPhotos(albumId: String): Result<List<AlbumPhoto>> = suspendCoroutine { continuation ->
        database.child("album_photos")
            .orderByChild("albumId")
            .equalTo(albumId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val photoList = mutableListOf<AlbumPhoto>()
                    for (photoSnapshot in snapshot.children) {
                        val photo = photoSnapshot.getValue(AlbumPhoto::class.java)
                        photo?.let {
                            if (it.id.isEmpty()) {
                                it.id = photoSnapshot.key ?: ""
                            }
                            photoList.add(it)
                        }
                    }
                    // Sort by order
                    continuation.resume(Result.success(photoList.sortedBy { it.order }))
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resume(Result.failure(Exception(error.message)))
                }
            })
    }

    // Add photo to album
    suspend fun addPhotoToAlbum(photo: AlbumPhoto): Result<Unit> {
        return try {
            val newRef = database.child("album_photos").push()
            val newPhoto = photo.copy(id = newRef.key ?: "")
            newRef.setValue(newPhoto).await()

            // Update photo count in album
            updateAlbumPhotoCount(photo.albumId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Delete photo from album
    suspend fun deletePhoto(photoId: String, albumId: String): Result<Unit> {
        return try {
            database.child("album_photos").child(photoId).removeValue().await()

            // Update photo count in album
            updateAlbumPhotoCount(albumId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
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
}
package com.example.banksampah.repository

import android.net.Uri
import android.util.Log
import com.example.banksampah.data.User
import com.example.banksampah.data.UserRole
import com.example.banksampah.viewmodel.uploadToCloudinary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ProfileRepository {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    private val TAG = "ProfileRepository"

    // Get current user data
    suspend fun getCurrentUser(): Result<User> = suspendCoroutine { continuation ->
        val uid = auth.currentUser?.uid

        if (uid == null) {
            continuation.resume(Result.failure(Exception("User not logged in")))
            return@suspendCoroutine
        }

        database.child("users").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        // Backward compatibility: jika role masih default tapi isAdmin true
                        if (user.role == UserRole.USER && user.isAdmin) {
                            user.role = UserRole.ADMIN
                        }
                        continuation.resume(Result.success(user))
                    } else {
                        continuation.resume(Result.failure(Exception("User data not found")))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resume(Result.failure(Exception(error.message)))
                }
            })
    }

    // Update display name (tanpa ubah fullName)
    suspend fun updateDisplayName(newDisplayName: String): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")

            database.child("users").child(uid).child("displayName")
                .setValue(newDisplayName)
                .await()

            Log.d(TAG, "Display name updated successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating display name", e)
            Result.failure(e)
        }
    }

    // Upload profile photo to Cloudinary
    suspend fun uploadProfilePhoto(imageUri: Uri): Result<String> = suspendCoroutine { continuation ->
        try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")

            Log.d(TAG, "Starting profile photo upload to Cloudinary for user: $uid")

            // Upload ke Cloudinary dengan folder profile_photos
            uploadToCloudinary(
                uri = imageUri,
                onSuccess = { downloadUrl ->
                    Log.d(TAG, "Profile photo uploaded successfully: $downloadUrl")
                    continuation.resume(Result.success(downloadUrl))
                },
                onError = { errorMessage ->
                    Log.e(TAG, "Error uploading profile photo: $errorMessage")
                    continuation.resume(Result.failure(Exception(errorMessage)))
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in uploadProfilePhoto", e)
            continuation.resume(Result.failure(e))
        }
    }

    // Update profile photo URL di database
    suspend fun updateProfilePhotoUrl(photoUrl: String): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")

            database.child("users").child(uid).child("profilePhotoUrl")
                .setValue(photoUrl)
                .await()

            Log.d(TAG, "Profile photo URL updated in database")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating profile photo URL", e)
            Result.failure(e)
        }
    }

    // Delete profile photo (hanya hapus URL dari database)
    // Note: Di Cloudinary, image tidak perlu dihapus karena auto-managed
    suspend fun deleteProfilePhoto(): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")

            // Hapus URL dari database
            database.child("users").child(uid).child("profilePhotoUrl")
                .setValue("")
                .await()

            Log.d(TAG, "Profile photo URL removed from database")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting profile photo", e)
            Result.failure(e)
        }
    }

    // Check if user is admin
    suspend fun isUserAdmin(): Result<Boolean> = suspendCoroutine { continuation ->
        val uid = auth.currentUser?.uid

        if (uid == null) {
            continuation.resume(Result.success(false))
            return@suspendCoroutine
        }

        database.child("users").child(uid).child("isAdmin")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val isAdmin = snapshot.getValue(Boolean::class.java) ?: false
                    continuation.resume(Result.success(isAdmin))
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resume(Result.success(false))
                }
            })
    }

    // Get user by ID (untuk keperluan admin atau melihat profile user lain)
    suspend fun getUserById(uid: String): Result<User> = suspendCoroutine { continuation ->
        database.child("users").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        continuation.resume(Result.success(user))
                    } else {
                        continuation.resume(Result.failure(Exception("User not found")))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resume(Result.failure(Exception(error.message)))
                }
            })
    }
}
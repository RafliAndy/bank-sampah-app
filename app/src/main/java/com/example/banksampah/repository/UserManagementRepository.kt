package com.example.banksampah.repository

import android.util.Log
import com.example.banksampah.data.User
import com.example.banksampah.data.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class UserManagementRepository {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    private val TAG = "UserManagementRepo"

    // Get all users (Admin only)
    suspend fun getAllUsers(): Result<List<User>> = suspendCoroutine { continuation ->
        database.child("users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val users = mutableListOf<User>()

                    for (userSnapshot in snapshot.children) {
                        val user = userSnapshot.getValue(User::class.java)
                        user?.let {
                            if (it.uid.isEmpty()) {
                                it.uid = userSnapshot.key ?: ""
                            }
                            users.add(it)
                        }
                    }

                    // Sort by role (Admin > Kader > User) then by name
                    val sorted = users.sortedWith(
                        compareByDescending<User> { it.getRoleType().level }
                            .thenBy { it.fullName }
                    )

                    continuation.resume(Result.success(sorted))
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resume(Result.failure(Exception(error.message)))
                }
            })
    }

    // Update user role (Admin only)
    suspend fun updateUserRole(userId: String, newRole: UserRole): Result<Unit> {
        return try {
            val currentUser = auth.currentUser?.uid

            // Don't allow changing own role
            if (currentUser == userId) {
                throw Exception("Tidak dapat mengubah role Anda sendiri")
            }

            // Update role
            val updates = mapOf(
                "role" to newRole.name,
                // Update isAdmin for backward compatibility
                "isAdmin" to (newRole == UserRole.ADMIN)
            )

            database.child("users").child(userId)
                .updateChildren(updates)
                .await()

            Log.d(TAG, "User role updated: $userId -> $newRole")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user role", e)
            Result.failure(e)
        }
    }

    // Delete user (Admin only)
    suspend fun deleteUser(userId: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser?.uid

            // Don't allow deleting own account
            if (currentUser == userId) {
                throw Exception("Tidak dapat menghapus akun Anda sendiri")
            }

            // Get user role before delete
            val userSnapshot = database.child("users").child(userId).get().await()
            val user = userSnapshot.getValue(User::class.java)

            // Don't allow deleting admin
            if (user?.getRoleType() == UserRole.ADMIN) {
                throw Exception("Tidak dapat menghapus akun Admin")
            }

            // Delete user data from database
            database.child("users").child(userId).removeValue().await()

            // Note: Firebase Auth user deletion requires admin SDK
            // For now, just mark as deleted in database

            Log.d(TAG, "User deleted: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user", e)
            Result.failure(e)
        }
    }

    // Suspend user (set to inactive)
    suspend fun suspendUser(userId: String, isSuspended: Boolean): Result<Unit> {
        return try {
            database.child("users").child(userId)
                .child("isSuspended")
                .setValue(isSuspended)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get user statistics
    suspend fun getUserStats(): Result<UserStats> = suspendCoroutine { continuation ->
        database.child("users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var totalUsers = 0
                    var adminCount = 0
                    var kaderCount = 0
                    var userCount = 0

                    for (userSnapshot in snapshot.children) {
                        val user = userSnapshot.getValue(User::class.java)
                        if (user != null) {
                            totalUsers++
                            when (user.getRoleType()) {
                                UserRole.ADMIN -> adminCount++
                                UserRole.KADER -> kaderCount++
                                UserRole.USER -> userCount++
                            }
                        }
                    }

                    val stats = UserStats(
                        total = totalUsers,
                        adminCount = adminCount,
                        kaderCount = kaderCount,
                        userCount = userCount
                    )

                    continuation.resume(Result.success(stats))
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resume(Result.failure(Exception(error.message)))
                }
            })
    }

    // Check if current user is admin
    suspend fun isCurrentUserAdmin(): Boolean {
        return try {
            val uid = auth.currentUser?.uid ?: return false
            val snapshot = database.child("users").child(uid).get().await()
            val user = snapshot.getValue(User::class.java)
            user?.getRoleType() == UserRole.ADMIN
        } catch (e: Exception) {
            false
        }
    }
}

data class UserStats(
    val total: Int = 0,
    val adminCount: Int = 0,
    val kaderCount: Int = 0,
    val userCount: Int = 0
)
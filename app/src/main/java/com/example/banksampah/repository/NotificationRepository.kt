package com.example.banksampah.repository

import android.util.Log
import com.example.banksampah.data.Notification
import com.example.banksampah.data.NotificationType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class NotificationRepository {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    private val TAG = "NotificationRepository"

    // Buat notifikasi ketika ada reply ke post
    suspend fun createPostReplyNotification(
        postId: String,
        postOwnerId: String,
        replyId: String
    ): Result<Unit> {
        return try {
            val currentUser = auth.currentUser ?: throw Exception("User not logged in")

            // Jangan buat notifikasi jika user reply ke post sendiri
            if (currentUser.uid == postOwnerId) {
                return Result.success(Unit)
            }

            // Ambil nama user yang reply
            val userSnapshot = database.child("users").child(currentUser.uid).get().await()
            val userName = userSnapshot.child("displayName").getValue(String::class.java)
                ?: userSnapshot.child("fullName").getValue(String::class.java)
                ?: "Seseorang"

            // Ambil judul post
            val postSnapshot = database.child("posts").child(postId).get().await()
            val postTitle = postSnapshot.child("title").getValue(String::class.java)
                ?: "post Anda"

            val notification = Notification(
                userId = postOwnerId,
                type = "FORUM_REPLY",
                title = "Balasan Baru di Forum",
                message = "$userName membalas post \"$postTitle\"",
                postId = postId,
                replyId = replyId,
                fromUserId = currentUser.uid,
                fromUserName = userName
            )

            val newRef = database.child("notifications").push()
            notification.id = newRef.key ?: ""
            newRef.setValue(notification).await()

            Log.d(TAG, "Post reply notification created for user: $postOwnerId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating post reply notification", e)
            Result.failure(e)
        }
    }

    // Buat notifikasi ketika ada reply ke reply (nested reply)
    suspend fun createNestedReplyNotification(
        postId: String,
        parentReplyId: String,
        parentReplyOwnerId: String,
        newReplyId: String
    ): Result<Unit> {
        return try {
            val currentUser = auth.currentUser ?: throw Exception("User not logged in")

            // Jangan buat notifikasi jika user reply ke reply sendiri
            if (currentUser.uid == parentReplyOwnerId) {
                return Result.success(Unit)
            }

            // Ambil nama user yang reply
            val userSnapshot = database.child("users").child(currentUser.uid).get().await()
            val userName = userSnapshot.child("displayName").getValue(String::class.java)
                ?: userSnapshot.child("fullName").getValue(String::class.java)
                ?: "Seseorang"

            val notification = Notification(
                userId = parentReplyOwnerId,
                type = "NESTED_REPLY",
                title = "Balasan Baru",
                message = "$userName membalas komentar Anda",
                postId = postId,
                replyId = newReplyId,
                fromUserId = currentUser.uid,
                fromUserName = userName
            )

            val newRef = database.child("notifications").push()
            notification.id = newRef.key ?: ""
            newRef.setValue(notification).await()

            Log.d(TAG, "Nested reply notification created for user: $parentReplyOwnerId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating nested reply notification", e)
            Result.failure(e)
        }
    }

    // Get notifications untuk current user
    suspend fun getUserNotifications(): Result<List<Notification>> = suspendCoroutine { continuation ->
        val userId = auth.currentUser?.uid

        if (userId == null) {
            continuation.resume(Result.failure(Exception("User not logged in")))
            return@suspendCoroutine
        }

        database.child("notifications")
            .orderByChild("userId")
            .equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val notifications = mutableListOf<Notification>()

                    for (notifSnapshot in snapshot.children) {
                        val notification = notifSnapshot.getValue(Notification::class.java)
                        notification?.let {
                            if (it.id.isEmpty()) {
                                it.id = notifSnapshot.key ?: ""
                            }
                            notifications.add(it)
                        }
                    }

                    // Sort by timestamp (newest first)
                    val sorted = notifications.sortedByDescending { it.timestamp }
                    continuation.resume(Result.success(sorted))
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resume(Result.failure(Exception(error.message)))
                }
            })
    }

    // Get unread notification count
    suspend fun getUnreadCount(): Result<Int> = suspendCoroutine { continuation ->
        val userId = auth.currentUser?.uid

        if (userId == null) {
            continuation.resume(Result.success(0))
            return@suspendCoroutine
        }

        database.child("notifications")
            .orderByChild("userId")
            .equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var count = 0

                    for (notifSnapshot in snapshot.children) {
                        val isRead = notifSnapshot.child("isRead").getValue(Boolean::class.java) ?: false
                        if (!isRead) count++
                    }

                    continuation.resume(Result.success(count))
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resume(Result.success(0))
                }
            })
    }

    // Mark notification as read
    suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            database.child("notifications").child(notificationId)
                .child("isRead")
                .setValue(true)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Mark all notifications as read
    suspend fun markAllAsRead(): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")

            val snapshot = database.child("notifications")
                .orderByChild("userId")
                .equalTo(userId)
                .get()
                .await()

            val updates = mutableMapOf<String, Any>()
            for (notifSnapshot in snapshot.children) {
                notifSnapshot.key?.let { key ->
                    updates["$key/isRead"] = true
                }
            }

            if (updates.isNotEmpty()) {
                database.child("notifications").updateChildren(updates).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Delete notification
    suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            database.child("notifications").child(notificationId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Delete all notifications for current user
    suspend fun deleteAllNotifications(): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")

            val snapshot = database.child("notifications")
                .orderByChild("userId")
                .equalTo(userId)
                .get()
                .await()

            for (notifSnapshot in snapshot.children) {
                notifSnapshot.ref.removeValue()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Listen for real-time notification updates
    fun listenToNotifications(
        onUpdate: (List<Notification>) -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            onError("User not logged in")
            return
        }

        database.child("notifications")
            .orderByChild("userId")
            .equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val notifications = mutableListOf<Notification>()

                    for (notifSnapshot in snapshot.children) {
                        val notification = notifSnapshot.getValue(Notification::class.java)
                        notification?.let {
                            if (it.id.isEmpty()) {
                                it.id = notifSnapshot.key ?: ""
                            }
                            notifications.add(it)
                        }
                    }

                    onUpdate(notifications.sortedByDescending { it.timestamp })
                }

                override fun onCancelled(error: DatabaseError) {
                    onError(error.message)
                }
            })
    }
}
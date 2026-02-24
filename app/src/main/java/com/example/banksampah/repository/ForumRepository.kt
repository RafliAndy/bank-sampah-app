package com.example.banksampah.repository

import android.util.Log
import com.example.banksampah.data.ForumPost
import com.example.banksampah.data.ForumReply
import com.example.banksampah.data.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ForumRepository {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    private val TAG = "ForumRepository"

    // Get all posts by user UID
    suspend fun getPostsByUser(uid: String): Result<List<ForumPost>> = suspendCoroutine { continuation ->
        database.child("posts")
            .orderByChild("uid")
            .equalTo(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val posts = mutableListOf<ForumPost>()

                    for (postSnapshot in snapshot.children) {
                        val post = postSnapshot.getValue(ForumPost::class.java)
                        post?.let {
                            if (it.id.isEmpty()) {
                                it.id = postSnapshot.key ?: ""
                            }
                            posts.add(it)
                        }
                    }

                    // Sort by timestamp (newest first)
                    val sortedPosts = posts.sortedByDescending { it.timestamp }

                    Log.d(TAG, "Found ${sortedPosts.size} posts for user $uid")
                    continuation.resume(Result.success(sortedPosts))
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Error getting posts: ${error.message}")
                    continuation.resume(Result.failure(Exception(error.message)))
                }
            })
    }

    // Get posts by current logged-in user
    suspend fun getMyPosts(): Result<List<ForumPost>> {
        val uid = auth.currentUser?.uid
        return if (uid != null) {
            getPostsByUser(uid)
        } else {
            Result.failure(Exception("User not logged in"))
        }
    }

    // Delete post (only owner or admin/kader can delete)
    suspend fun deletePost(postId: String): Result<Unit> {
        return try {
            val currentUid = auth.currentUser?.uid ?: throw Exception("User not logged in")

            // Get post to check ownership
            val postSnapshot = database.child("posts").child(postId).get().await()
            val post = postSnapshot.getValue(ForumPost::class.java)

            if (post == null) {
                throw Exception("Post not found")
            }

            // Check if user is owner, ADMIN, or KADER
            val isOwner = post.uid == currentUid
            val canDelete = canDeletePost(currentUid)

            if (!isOwner && !canDelete) {
                throw Exception("You don't have permission to delete this post")
            }

            // Delete post
            database.child("posts").child(postId).removeValue().await()

            // Also delete all replies for this post
            deleteRepliesForPost(postId)

            Log.d(TAG, "Post $postId deleted successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting post: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Delete reply (owner, admin, atau kader dapat menghapus)
    suspend fun deleteReply(replyId: String, postId: String): Result<Unit> {
        return try {
            val currentUid = auth.currentUser?.uid ?: throw Exception("User not logged in")

            // Get reply to check ownership
            val replySnapshot = database.child("replies").child(replyId).get().await()
            val reply = replySnapshot.getValue(ForumReply::class.java)

            if (reply == null) {
                throw Exception("Reply not found")
            }

            // Check if user is owner, ADMIN, or KADER
            val isOwner = reply.uid == currentUid
            val canDelete = canDeletePost(currentUid) // Gunakan fungsi yang sudah ada

            if (!isOwner && !canDelete) {
                throw Exception("You don't have permission to delete this reply")
            }

            // Dapatkan role untuk tracking
            val userSnapshot = database.child("users").child(currentUid).get().await()
            val roleString = userSnapshot.child("role").getValue(String::class.java)
            val role = try {
                UserRole.valueOf(roleString ?: "USER")
            } catch (e: Exception) {
                UserRole.USER
            }

            // Soft delete: Mark sebagai deleted dengan info siapa yang hapus
            val deletedBy = when {
                isOwner -> "owner"
                role == UserRole.ADMIN -> "admin"
                role == UserRole.KADER -> "kader"
                else -> "user"
            }

            database.child("replies").child(replyId).updateChildren(mapOf(
                "isDeleted" to true,
                "deletedBy" to deletedBy,
                "deletedAt" to System.currentTimeMillis(),
                "body" to ""
            )).await()

            Log.d(TAG, "Reply $replyId soft deleted by $deletedBy")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting reply: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Hard delete (hanya untuk owner atau yang menghapusnya bisa hapus sepenuhnya)
    suspend fun hardDeleteReply(replyId: String): Result<Unit> {
        return try {
            val currentUid = auth.currentUser?.uid ?: throw Exception("User not logged in")

            // Get reply
            val replySnapshot = database.child("replies").child(replyId).get().await()
            val reply = replySnapshot.getValue(ForumReply::class.java)

            if (reply == null) {
                throw Exception("Reply not found")
            }

            // Hanya owner atau yang originally delete bisa hard delete
            val isOwner = reply.uid == currentUid
            val isDeleter = reply.deletedBy.isNotEmpty() // Ada yang sudah delete sebelumnya

            if (!isOwner && !isDeleter) {
                throw Exception("Only owner can hard delete")
            }

            // Hard delete: hapus sepenuhnya beserta nested replies
            deleteNestedReplies(replyId)
            database.child("replies").child(replyId).removeValue().await()

            Log.d(TAG, "Reply $replyId hard deleted")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error hard deleting reply: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Hapus semua nested replies
    private suspend fun deleteNestedReplies(parentReplyId: String) {
        try {
            val repliesSnapshot = database.child("replies")
                .orderByChild("parentReplyId")
                .equalTo(parentReplyId)
                .get()
                .await()

            for (replySnapshot in repliesSnapshot.children) {
                val nestedId = replySnapshot.key ?: continue
                deleteNestedReplies(nestedId) // Recursive
                replySnapshot.ref.removeValue().await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting nested replies: ${e.message}", e)
        }
    }



    // Delete all replies for a post
    private suspend fun deleteRepliesForPost(postId: String) {
        try {
            val repliesSnapshot = database.child("replies")
                .orderByChild("postId")
                .equalTo(postId)
                .get()
                .await()

            val deleteTasks = mutableListOf<com.google.android.gms.tasks.Task<Void>>()

            for (replySnapshot in repliesSnapshot.children) {
                replySnapshot.key?.let { replyId ->
                    deleteTasks.add(database.child("replies").child(replyId).removeValue())
                }
            }

            // Wait for all deletes to complete
            com.google.android.gms.tasks.Tasks.whenAll(deleteTasks).await()

            Log.d(TAG, "Deleted ${deleteTasks.size} replies for post $postId")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting replies: ${e.message}", e)
        }
    }

    // Check if user is admin/kader
    private suspend fun canDeletePost(uid: String): Boolean {
        return try {
            val snapshot = database.child("users").child(uid).get().await()

            // Check dengan role field (newer approach)
            val roleString = snapshot.child("role").getValue(String::class.java)
            if (roleString != null) {
                val role = try {
                    UserRole.valueOf(roleString)
                } catch (e: Exception) {
                    UserRole.USER
                }
                return role == UserRole.ADMIN || role == UserRole.KADER
            }

            // Fallback untuk backward compatibility: check isAdmin
            val isAdmin = snapshot.child("isAdmin").getValue(Boolean::class.java) ?: false
            isAdmin
        } catch (e: Exception) {
            Log.e(TAG, "Error checking user role: ${e.message}")
            false
        }
    }

    // Get single post by ID
    suspend fun getPostById(postId: String): Result<ForumPost> {
        return try {
            val snapshot = database.child("posts").child(postId).get().await()
            val post = snapshot.getValue(ForumPost::class.java)

            if (post != null) {
                if (post.id.isEmpty()) {
                    post.id = snapshot.key ?: postId
                }
                Result.success(post)
            } else {
                Result.failure(Exception("Post not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting post: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Count posts by user
    suspend fun countUserPosts(uid: String): Result<Int> = suspendCoroutine { continuation ->
        database.child("posts")
            .orderByChild("uid")
            .equalTo(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val count = snapshot.childrenCount.toInt()
                    continuation.resume(Result.success(count))
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resume(Result.success(0))
                }
            })
    }
}
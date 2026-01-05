package com.example.banksampah.repository

import android.util.Log
import com.example.banksampah.data.ForumPost
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

    // Delete post (only owner or admin can delete)
    suspend fun deletePost(postId: String): Result<Unit> {
        return try {
            val currentUid = auth.currentUser?.uid ?: throw Exception("User not logged in")

            // Get post to check ownership
            val postSnapshot = database.child("posts").child(postId).get().await()
            val post = postSnapshot.getValue(ForumPost::class.java)

            if (post == null) {
                throw Exception("Post not found")
            }

            // Check if user is owner or admin
            val isOwner = post.uid == currentUid
            val isAdmin = checkIfAdmin(currentUid)

            if (!isOwner && !isAdmin) {
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

    // Check if user is admin
    private suspend fun checkIfAdmin(uid: String): Boolean {
        return try {
            val snapshot = database.child("users").child(uid).child("isAdmin").get().await()
            snapshot.getValue(Boolean::class.java) ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking admin status: ${e.message}")
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
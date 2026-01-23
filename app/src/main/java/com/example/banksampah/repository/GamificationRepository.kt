package com.example.banksampah.repository

import android.util.Log
import com.example.banksampah.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class GamificationRepository {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    private val TAG = "GamificationRepo"

    // ========== VOTING SYSTEM (ENHANCED) ==========

    suspend fun vote(targetId: String, targetType: VoteType, voteValue: Int): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid
            if (uid == null) {
                Log.e(TAG, "User not authenticated")
                return Result.failure(Exception("Anda harus login terlebih dahulu"))
            }

            Log.d(TAG, "Attempting vote: targetId=$targetId, type=$targetType, value=$voteValue, userId=$uid")

            // Check if already voted
            val existingVote = getExistingVote(uid, targetId, targetType)

            if (existingVote != null) {
                Log.d(TAG, "Found existing vote: ${existingVote.id}, current value: ${existingVote.voteValue}")

                // Update existing vote
                if (existingVote.voteValue == voteValue) {
                    // Remove vote (toggle off)
                    Log.d(TAG, "Removing vote (toggle off)")
                    deleteVote(existingVote.id)
                    updateVoteCount(targetId, targetType, -voteValue)
                } else {
                    // Change vote (from upvote to downvote or vice versa)
                    Log.d(TAG, "Changing vote direction")
                    existingVote.voteValue = voteValue
                    database.child("votes").child(existingVote.id)
                        .setValue(existingVote).await()
                    // Update count: remove old vote effect + add new vote effect
                    updateVoteCount(targetId, targetType, voteValue * 2)
                }
            } else {
                Log.d(TAG, "Creating new vote")
                // Create new vote
                val vote = Vote(
                    targetId = targetId,
                    targetType = targetType,
                    voterId = uid,
                    voteValue = voteValue
                )
                val newRef = database.child("votes").push()
                vote.id = newRef.key ?: ""

                // Set value with error handling
                newRef.setValue(vote).await()
                Log.d(TAG, "Vote created successfully with ID: ${vote.id}")

                updateVoteCount(targetId, targetType, voteValue)

                // Award points to post/reply author
                awardPointsForVote(targetId, targetType, voteValue)
            }

            Log.d(TAG, "Vote operation completed successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error voting: ${e.message}", e)

            // Provide more specific error messages
            val userFriendlyMessage = when {
                e.message?.contains("Permission denied") == true ->
                    "Izin ditolak. Pastikan Anda sudah login dan Firebase Rules sudah benar."
                e.message?.contains("network") == true ->
                    "Koneksi internet bermasalah. Coba lagi."
                else ->
                    "Gagal memberi vote: ${e.message}"
            }

            Result.failure(Exception(userFriendlyMessage))
        }
    }

    private suspend fun getExistingVote(voterId: String, targetId: String, targetType: VoteType): Vote? {
        return try {
            Log.d(TAG, "Checking existing vote for voterId=$voterId, targetId=$targetId")

            val snapshot = database.child("votes")
                .orderByChild("voterId")
                .equalTo(voterId)
                .get().await()

            for (voteSnap in snapshot.children) {
                val vote = voteSnap.getValue(Vote::class.java)
                if (vote != null && vote.id.isEmpty()) {
                    vote.id = voteSnap.key ?: ""
                }

                if (vote?.targetId == targetId && vote.targetType == targetType) {
                    Log.d(TAG, "Found existing vote: ${vote.id}")
                    return vote
                }
            }

            Log.d(TAG, "No existing vote found")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error checking existing vote: ${e.message}", e)
            null
        }
    }

    private suspend fun deleteVote(voteId: String) {
        try {
            Log.d(TAG, "Deleting vote: $voteId")
            database.child("votes").child(voteId).removeValue().await()
            Log.d(TAG, "Vote deleted successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting vote: ${e.message}", e)
            throw e
        }
    }

    private suspend fun updateVoteCount(targetId: String, targetType: VoteType, change: Int) {
        try {
            val path = if (targetType == VoteType.POST) "posts" else "replies"
            val field = if (change > 0) "upvotes" else "downvotes"

            Log.d(TAG, "Updating vote count: path=$path, targetId=$targetId, field=$field, change=$change")

            val ref = database.child(path).child(targetId).child(field)
            val current = ref.get().await().getValue(Int::class.java) ?: 0
            val newValue = maxOf(0, current + kotlin.math.abs(change)) // Prevent negative values

            ref.setValue(newValue).await()
            Log.d(TAG, "Vote count updated: $current -> $newValue")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating vote count: ${e.message}", e)
            throw e
        }
    }

    private suspend fun awardPointsForVote(targetId: String, targetType: VoteType, voteValue: Int) {
        try {
            if (voteValue < 0) {
                Log.d(TAG, "Skipping points award for downvote")
                return // No points for downvotes
            }

            val path = if (targetType == VoteType.POST) "posts" else "replies"
            val authorSnapshot = database.child(path).child(targetId).child("uid").get().await()
            val authorUid = authorSnapshot.getValue(String::class.java)

            if (authorUid == null) {
                Log.w(TAG, "Author UID not found for target: $targetId")
                return
            }

            val points = if (targetType == VoteType.POST) 3 else 2
            val reason = if (targetType == VoteType.POST)
                PointReasons.POST_UPVOTED else PointReasons.REPLY_UPVOTED

            Log.d(TAG, "Awarding $points points to $authorUid for $reason")
            addPoints(authorUid, points, reason)
        } catch (e: Exception) {
            Log.e(TAG, "Error awarding points for vote: ${e.message}", e)
            // Don't throw - points are bonus, vote should still succeed
        }
    }

    // ========== USER GAMIFICATION ==========

    suspend fun getUserGamification(uid: String): Result<UserGamification> =
        suspendCoroutine { continuation ->
            database.child("gamification").child(uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val gamification = snapshot.getValue(UserGamification::class.java)
                            ?: UserGamification(uid = uid)
                        continuation.resume(Result.success(gamification))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(Result.failure(Exception(error.message)))
                    }
                })
        }

    suspend fun updateUserGamification(gamification: UserGamification): Result<Unit> {
        return try {
            database.child("gamification").child(gamification.uid)
                .setValue(gamification).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== POINTS SYSTEM ==========

    suspend fun addPoints(uid: String, points: Int, reason: String): Result<Unit> {
        return try {
            val gamification = getUserGamification(uid).getOrThrow()
            val newPoints = gamification.totalPoints + points
            val newLevel = LevelSystem.calculateLevel(newPoints)

            val updated = gamification.copy(
                totalPoints = newPoints,
                level = newLevel
            )

            updateUserGamification(updated)

            // Log transaction
            logPointTransaction(uid, points, reason)

            // Check for new badges
            checkAndAwardBadges(uid)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding points", e)
            Result.failure(e)
        }
    }

    private suspend fun logPointTransaction(uid: String, points: Int, reason: String) {
        try {
            val transaction = PointTransaction(
                uid = uid,
                points = points,
                reason = reason
            )
            val newRef = database.child("point_transactions").push()
            transaction.id = newRef.key ?: ""
            newRef.setValue(transaction).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error logging point transaction: ${e.message}", e)
            // Don't throw - transaction log is optional
        }
    }

    // ========== HELPFUL ANSWER SYSTEM ==========

    suspend fun markReplyAsHelpful(postId: String, replyId: String): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("Not logged in")

            // Verify user is post owner
            val postSnapshot = database.child("posts").child(postId).get().await()
            val postOwnerId = postSnapshot.child("uid").getValue(String::class.java)

            if (postOwnerId != uid) {
                throw Exception("Only post owner can mark helpful answers")
            }

            // Mark reply as helpful
            database.child("replies").child(replyId).child("isMarkedHelpful")
                .setValue(true).await()

            // Award points to reply author
            val replyAuthorSnapshot = database.child("replies").child(replyId).child("uid").get().await()
            val replyAuthorUid = replyAuthorSnapshot.getValue(String::class.java) ?: return Result.success(Unit)

            addPoints(replyAuthorUid, 15, PointReasons.HELPFUL_ANSWER)

            // Update helpful answer count
            val gamification = getUserGamification(replyAuthorUid).getOrThrow()
            updateUserGamification(gamification.copy(
                helpfulAnswerCount = gamification.helpfulAnswerCount + 1
            ))

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error marking helpful", e)
            Result.failure(e)
        }
    }

    // ========== BADGE SYSTEM ==========

    private suspend fun checkAndAwardBadges(uid: String) {
        try {
            val gamification = getUserGamification(uid).getOrThrow()
            val currentBadges = gamification.badges.toMutableList()
            var newBadges = false

            for (badge in BadgeDefinitions.ALL_BADGES) {
                if (currentBadges.contains(badge.id)) continue

                val earned = when (val req = badge.requirement) {
                    is BadgeRequirement.FirstPost -> gamification.postCount >= req.required
                    is BadgeRequirement.TotalReplies -> gamification.replyCount >= req.required
                    is BadgeRequirement.TotalPoints -> gamification.totalPoints >= req.required
                    is BadgeRequirement.HelpfulAnswers -> gamification.helpfulAnswerCount >= req.required
                    is BadgeRequirement.LoginStreak -> gamification.longestStreak >= req.required
                    is BadgeRequirement.PostsWithImage -> {
                        val postsWithImage = countPostsWithImage(uid)
                        postsWithImage >= req.required
                    }
                }

                if (earned) {
                    currentBadges.add(badge.id)
                    newBadges = true
                    Log.d(TAG, "Badge earned: ${badge.name}")
                }
            }

            if (newBadges) {
                updateUserGamification(gamification.copy(badges = currentBadges))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking badges: ${e.message}", e)
            // Don't throw - badge checking is optional
        }
    }

    private suspend fun countPostsWithImage(uid: String): Int {
        return try {
            val snapshot = database.child("posts")
                .orderByChild("uid")
                .equalTo(uid)
                .get().await()

            snapshot.children.count {
                val imageUrl = it.child("imageUrl").getValue(String::class.java)
                !imageUrl.isNullOrEmpty()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error counting posts with image: ${e.message}", e)
            0
        }
    }

    // ========== LEADERBOARD ==========

    suspend fun getLeaderboard(limit: Int = 10): Result<List<LeaderboardEntry>> =
        suspendCoroutine { continuation ->
            database.child("gamification")
                .orderByChild("totalPoints")
                .limitToLast(limit)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val entries = mutableListOf<LeaderboardEntry>()
                        val totalCount = snapshot.childrenCount.toInt()
                        var processedCount = 0

                        if (totalCount == 0) {
                            continuation.resume(Result.success(emptyList()))
                            return
                        }

                        for ((index, child) in snapshot.children.reversed().withIndex()) {
                            val gamification = child.getValue(UserGamification::class.java) ?: continue

                            database.child("users").child(gamification.uid).get()
                                .addOnSuccessListener { userSnapshot ->
                                    val displayName = userSnapshot.child("displayName").getValue(String::class.java) ?: "Unknown"
                                    val photoUrl = userSnapshot.child("profilePhotoUrl").getValue(String::class.java) ?: ""

                                    entries.add(LeaderboardEntry(
                                        uid = gamification.uid,
                                        displayName = displayName,
                                        profilePhotoUrl = photoUrl,
                                        totalPoints = gamification.totalPoints,
                                        level = gamification.level,
                                        badges = gamification.badges,
                                        rank = index + 1
                                    ))

                                    processedCount++
                                    if (processedCount == totalCount) {
                                        continuation.resume(Result.success(entries.sortedBy { it.rank }))
                                    }
                                }
                                .addOnFailureListener {
                                    processedCount++
                                    if (processedCount == totalCount) {
                                        continuation.resume(Result.success(entries.sortedBy { it.rank }))
                                    }
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(Result.failure(Exception(error.message)))
                    }
                })
        }

    // ========== DAILY LOGIN STREAK ==========

    suspend fun updateLoginStreak(uid: String): Result<Unit> {
        return try {
            val gamification = getUserGamification(uid).getOrThrow()
            val today = System.currentTimeMillis() / (24 * 60 * 60 * 1000)
            val lastLogin = gamification.lastLoginDate / (24 * 60 * 60 * 1000)

            val newStreak = when {
                lastLogin == 0L -> 1
                today - lastLogin == 1L -> gamification.currentStreak + 1
                today == lastLogin -> gamification.currentStreak
                else -> 1
            }

            val updated = gamification.copy(
                currentStreak = newStreak,
                longestStreak = maxOf(gamification.longestStreak, newStreak),
                lastLoginDate = System.currentTimeMillis()
            )

            updateUserGamification(updated)

            if (newStreak > 1) {
                addPoints(uid, newStreak, PointReasons.STREAK_BONUS)
            } else {
                addPoints(uid, 1, PointReasons.DAILY_LOGIN)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
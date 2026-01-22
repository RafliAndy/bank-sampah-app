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
        val transaction = PointTransaction(
            uid = uid,
            points = points,
            reason = reason
        )
        val newRef = database.child("point_transactions").push()
        transaction.id = newRef.key ?: ""
        newRef.setValue(transaction).await()
    }

    // ========== VOTING SYSTEM ==========

    suspend fun vote(targetId: String, targetType: VoteType, voteValue: Int): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("Not logged in")

            // Check if already voted
            val existingVote = getExistingVote(uid, targetId, targetType)

            if (existingVote != null) {
                // Update existing vote
                if (existingVote.voteValue == voteValue) {
                    // Remove vote (toggle off)
                    deleteVote(existingVote.id)
                    updateVoteCount(targetId, targetType, -voteValue)
                } else {
                    // Change vote
                    existingVote.voteValue = voteValue
                    database.child("votes").child(existingVote.id)
                        .setValue(existingVote).await()
                    updateVoteCount(targetId, targetType, voteValue * 2)
                }
            } else {
                // Create new vote
                val vote = Vote(
                    targetId = targetId,
                    targetType = targetType,
                    voterId = uid,
                    voteValue = voteValue
                )
                val newRef = database.child("votes").push()
                vote.id = newRef.key ?: ""
                newRef.setValue(vote).await()

                updateVoteCount(targetId, targetType, voteValue)

                // Award points to post/reply author
                awardPointsForVote(targetId, targetType, voteValue)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error voting", e)
            Result.failure(e)
        }
    }

    private suspend fun getExistingVote(voterId: String, targetId: String, targetType: VoteType): Vote? {
        val snapshot = database.child("votes")
            .orderByChild("voterId")
            .equalTo(voterId)
            .get().await()

        for (voteSnap in snapshot.children) {
            val vote = voteSnap.getValue(Vote::class.java)
            if (vote?.targetId == targetId && vote.targetType == targetType) {
                return vote
            }
        }
        return null
    }

    private suspend fun deleteVote(voteId: String) {
        database.child("votes").child(voteId).removeValue().await()
    }

    private suspend fun updateVoteCount(targetId: String, targetType: VoteType, change: Int) {
        val path = if (targetType == VoteType.POST) "posts" else "replies"
        val field = if (change > 0) "upvotes" else "downvotes"

        val ref = database.child(path).child(targetId).child(field)
        val current = ref.get().await().getValue(Int::class.java) ?: 0
        ref.setValue(current + kotlin.math.abs(change))
    }

    private suspend fun awardPointsForVote(targetId: String, targetType: VoteType, voteValue: Int) {
        if (voteValue < 0) return // No points for downvotes

        val path = if (targetType == VoteType.POST) "posts" else "replies"
        val authorSnapshot = database.child(path).child(targetId).child("uid").get().await()
        val authorUid = authorSnapshot.getValue(String::class.java) ?: return

        val points = if (targetType == VoteType.POST) 3 else 2
        val reason = if (targetType == VoteType.POST)
            PointReasons.POST_UPVOTED else PointReasons.REPLY_UPVOTED

        addPoints(authorUid, points, reason)
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
                    // Count posts with images
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
    }

    private suspend fun countPostsWithImage(uid: String): Int {
        val snapshot = database.child("posts")
            .orderByChild("uid")
            .equalTo(uid)
            .get().await()

        return snapshot.children.count {
            val imageUrl = it.child("imageUrl").getValue(String::class.java)
            !imageUrl.isNullOrEmpty()
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

                        for ((index, child) in snapshot.children.reversed().withIndex()) {
                            val gamification = child.getValue(UserGamification::class.java) ?: continue

                            // Get user display name
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

                                    if (entries.size == snapshot.childrenCount.toInt()) {
                                        continuation.resume(Result.success(entries.sortedBy { it.rank }))
                                    }
                                }
                        }

                        if (snapshot.childrenCount == 0L) {
                            continuation.resume(Result.success(emptyList()))
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
            val today = System.currentTimeMillis() / (24 * 60 * 60 * 1000) // Days since epoch
            val lastLogin = gamification.lastLoginDate / (24 * 60 * 60 * 1000)

            val newStreak = when {
                lastLogin == 0L -> 1 // First login
                today - lastLogin == 1L -> gamification.currentStreak + 1 // Consecutive day
                today == lastLogin -> gamification.currentStreak // Same day
                else -> 1 // Streak broken
            }

            val updated = gamification.copy(
                currentStreak = newStreak,
                longestStreak = maxOf(gamification.longestStreak, newStreak),
                lastLoginDate = System.currentTimeMillis()
            )

            updateUserGamification(updated)

            // Award streak bonus points
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
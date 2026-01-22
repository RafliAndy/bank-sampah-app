package com.example.banksampah.data

// ========== USER GAMIFICATION DATA ==========
data class UserGamification(
    var uid: String = "",
    var totalPoints: Int = 0,
    var level: Int = 1,
    var postCount: Int = 0,
    var replyCount: Int = 0,
    var helpfulAnswerCount: Int = 0,
    var currentStreak: Int = 0,
    var longestStreak: Int = 0,
    var lastLoginDate: Long = 0,
    var badges: List<String> = emptyList(), // List of badge IDs
    var createdAt: Long = System.currentTimeMillis()
)

// ========== BADGE SYSTEM ==========
data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val icon: String, // emoji atau icon identifier
    val requirement: BadgeRequirement
)

sealed class BadgeRequirement {
    data class FirstPost(val required: Int = 1) : BadgeRequirement()
    data class TotalReplies(val required: Int) : BadgeRequirement()
    data class TotalPoints(val required: Int) : BadgeRequirement()
    data class HelpfulAnswers(val required: Int) : BadgeRequirement()
    data class LoginStreak(val required: Int) : BadgeRequirement()
    data class PostsWithImage(val required: Int) : BadgeRequirement()
}

// Predefined badges
object BadgeDefinitions {
    val ALL_BADGES = listOf(
        Badge("beginner", "🌱 Pemula", "Buat post pertama", "🌱",
            BadgeRequirement.FirstPost()),
        Badge("communicator", "💬 Komunikator", "Buat 10 reply", "💬",
            BadgeRequirement.TotalReplies(10)),
        Badge("active_contributor", "⭐ Kontributor Aktif", "Raih 50 poin", "⭐",
            BadgeRequirement.TotalPoints(50)),
        Badge("expert", "🏆 Expert", "Raih 200 poin", "🏆",
            BadgeRequirement.TotalPoints(200)),
        Badge("streak_master", "🔥 Streak Master", "Login 7 hari berturut-turut", "🔥",
            BadgeRequirement.LoginStreak(7)),
        Badge("top_helper", "👑 Top Helper", "10 jawaban ditandai helpful", "👑",
            BadgeRequirement.HelpfulAnswers(10)),
        Badge("visual_storyteller", "📸 Visual Storyteller", "Upload 5 post dengan gambar", "📸",
            BadgeRequirement.PostsWithImage(5))
    )
}

// ========== VOTING SYSTEM ==========
data class Vote(
    var id: String = "",
    var targetId: String = "", // post ID atau reply ID
    var targetType: VoteType = VoteType.POST,
    var voterId: String = "",
    var voteValue: Int = 0, // 1 for upvote, -1 for downvote
    var timestamp: Long = System.currentTimeMillis()
)

enum class VoteType {
    POST, REPLY
}

// ========== UPDATED FORUM POST (tambah voting) ==========
data class ForumPostGamified(
    var id: String = "",
    var title: String = "",
    var body: String = "",
    var imageUrl: String? = null,
    var uid: String = "",
    var authorName: String = "",
    var timestamp: Long = 0,
    var upvotes: Int = 0,
    var downvotes: Int = 0,
    var isHelpful: Boolean = false,
    var hasImage: Boolean = false
)

// ========== UPDATED FORUM REPLY (tambah voting & helpful) ==========
data class ForumReplyGamified(
    var id: String = "",
    var postId: String = "",
    var parentReplyId: String? = null,
    var body: String = "",
    var uid: String = "",
    var authorName: String = "",
    var timestamp: Long = 0,
    var level: Int = 0,
    var upvotes: Int = 0,
    var downvotes: Int = 0,
    var isMarkedHelpful: Boolean = false
)

// ========== LEADERBOARD ENTRY ==========
data class LeaderboardEntry(
    val uid: String = "",
    val displayName: String = "",
    val profilePhotoUrl: String = "",
    val totalPoints: Int = 0,
    val level: Int = 1,
    val badges: List<String> = emptyList(),
    val rank: Int = 0
)

// ========== POINT TRANSACTION LOG ==========
data class PointTransaction(
    var id: String = "",
    var uid: String = "",
    var points: Int = 0,
    var reason: String = "",
    var timestamp: Long = System.currentTimeMillis()
)

// Point earning reasons
object PointReasons {
    const val CREATE_POST = "Membuat post baru"
    const val CREATE_REPLY = "Membuat reply"
    const val POST_UPVOTED = "Post mendapat upvote"
    const val REPLY_UPVOTED = "Reply mendapat upvote"
    const val HELPFUL_ANSWER = "Reply ditandai sebagai helpful"
    const val DAILY_LOGIN = "Login harian"
    const val STREAK_BONUS = "Bonus login berturut-turut"
}

// ========== LEVEL SYSTEM ==========
object LevelSystem {
    // Points required for each level
    val LEVEL_POINTS = mapOf(
        1 to 0,
        2 to 10,
        3 to 30,
        4 to 60,
        5 to 100,
        6 to 150,
        7 to 210,
        8 to 280,
        9 to 360,
        10 to 450
    )

    fun calculateLevel(points: Int): Int {
        return LEVEL_POINTS.entries
            .filter { points >= it.value }
            .maxByOrNull { it.key }
            ?.key ?: 1
    }

    fun getPointsForNextLevel(currentLevel: Int): Int {
        return LEVEL_POINTS[currentLevel + 1] ?: LEVEL_POINTS[10]!!
    }

    fun getProgressToNextLevel(points: Int, currentLevel: Int): Float {
        val currentLevelPoints = LEVEL_POINTS[currentLevel] ?: 0
        val nextLevelPoints = getPointsForNextLevel(currentLevel)
        val progress = points - currentLevelPoints
        val required = nextLevelPoints - currentLevelPoints
        return if (required > 0) progress.toFloat() / required.toFloat() else 1f
    }
}
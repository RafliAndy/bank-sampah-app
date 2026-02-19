package com.example.banksampah.data

// Models untuk Level Perks
data class LevelPerk(
    val id: String,
    val level: Int,
    val title: String,
    val description: String,
    val icon: String, // emoji
    val category: PerkCategory
)

enum class PerkCategory {
    FORUM,      // Fitur forum
    PROFILE,    // Profile customization
    BADGE,      // Badges
    EXCLUSIVE   // Fitur eksklusif
}

// Data untuk perks yang tersedia
object LevelPerksData {
    val ALL_PERKS = listOf(
        // Level 1 - Dasar
        LevelPerk(
            id = "basic_forum",
            level = 1,
            title = "Akses Forum",
            description = "Bisa membaca dan berpartisipasi di forum komunitas",
            icon = "💬",
            category = PerkCategory.FORUM
        ),

        // Level 2
        LevelPerk(
            id = "upload_forum",
            level = 2,
            title = "Upload Gambar",
            description = "Bisa upload gambar di post dan reply forum",
            icon = "📸",
            category = PerkCategory.FORUM
        ),

        // Level 3
        LevelPerk(
            id = "custom_profile",
            level = 3,
            title = "Profile Warna",
            description = "Ubah warna profil Anda dengan tema pilihan",
            icon = "🎨",
            category = PerkCategory.PROFILE
        ),

        // Level 5
        LevelPerk(
            id = "badge_collector",
            level = 5,
            title = "Badge Kolektor",
            description = "Unlock badge eksklusif Level 5",
            icon = "🏅",
            category = PerkCategory.BADGE
        ),

        // Level 7
        LevelPerk(
            id = "custom_title",
            level = 7,
            title = "Custom Title",
            description = "Buat custom title di profil Anda (maks 20 karakter)",
            icon = "✨",
            category = PerkCategory.PROFILE
        ),

        // Level 10
        LevelPerk(
            id = "mentor_badge",
            level = 10,
            title = "Mentor Badge",
            description = "Badge khusus Mentor - tampil dengan lencana eksklusif",
            icon = "👑",
            category = PerkCategory.BADGE
        )
    )

    fun getPerksUntilLevel(level: Int): List<LevelPerk> {
        return ALL_PERKS.filter { it.level <= level }
    }

    fun getNewPerksAtLevel(level: Int): List<LevelPerk> {
        return ALL_PERKS.filter { it.level == level }
    }

    fun getPerksByCategory(category: PerkCategory): List<LevelPerk> {
        return ALL_PERKS.filter { it.category == category }
    }
}

// User's unlocked perks
data class UserUnlockedPerks(
    var uid: String = "",
    var unlockedPerkIds: List<String> = emptyList(),
    var customTitle: String = "",
    var profileColor: String = "#4CAF50", // Default green
    var lastUpdated: Long = System.currentTimeMillis()
)
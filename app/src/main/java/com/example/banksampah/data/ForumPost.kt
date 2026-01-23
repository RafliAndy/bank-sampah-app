package com.example.banksampah.data

data class ForumPost(
    var id: String = "",
    var title: String = "",
    var body: String = "",
    var imageUrl: String? = null,
    var uid: String = "",
    var authorName: String = "",
    var timestamp: Long = 0,
    var upvotes: Int = 0,
    var downvotes: Int = 0,
    var hasImage: Boolean = false,

    // ✅ TAMBAHAN: Kategori
    var category: String = "GENERAL" // Default kategori
)

// ✅ Enum untuk Kategori Forum
enum class ForumCategory(val displayName: String, val color: Long) {
    GENERAL("Umum", 0xFF9E9E9E),
    QUESTION("Tanya Jawab", 0xFF2196F3),
    DISCUSSION("Diskusi", 0xFFFF9800),
    INFO("Informasi", 0xFF4CAF50),
    TIPS("Tips & Trik", 0xFF9C27B0),
    NEWS("Berita", 0xFFF44336);

    companion object {
        fun fromString(value: String): ForumCategory {
            return values().find { it.name == value } ?: GENERAL
        }

        fun getAllCategories(): List<ForumCategory> = values().toList()
    }
}
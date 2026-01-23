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
    var hasImage: Boolean = false
)

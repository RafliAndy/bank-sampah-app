package com.example.banksampah.data

data class ForumReply(
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
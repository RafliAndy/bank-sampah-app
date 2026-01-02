package com.example.banksampah.data

data class ForumReply(
    var id: String = "",
    var postId: String = "", // ID post yang dibalas
    var parentReplyId: String? = null, // Null jika balasan langsung ke post, berisi ID jika balasan ke balasan
    var body: String = "",
    var uid: String = "",
    var authorName: String = "",
    var timestamp: Long = 0,
    var level: Int = 0 // 0 untuk balasan langsung, 1+ untuk nested replies
)
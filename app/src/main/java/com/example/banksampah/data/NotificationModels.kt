package com.example.banksampah.data

import com.google.firebase.database.PropertyName

data class Notification(
    var id: String = "",
    var userId: String = "", // User yang menerima notifikasi
    var type: String = "FORUM_REPLY", // Ubah jadi String untuk Firebase
    var title: String = "",
    var message: String = "",
    var postId: String = "", // ID post terkait
    var replyId: String = "", // ID reply terkait (jika ada)
    var fromUserId: String = "", // User yang membuat notifikasi
    var fromUserName: String = "", // Nama user yang membuat notifikasi
    var timestamp: Long = System.currentTimeMillis(),

    @get:PropertyName("isRead")
    @set:PropertyName("isRead")
    var isRead: Boolean = false
) {
    // Helper untuk konversi type
    fun getNotificationType(): NotificationType {
        return try {
            NotificationType.valueOf(type)
        } catch (e: Exception) {
            NotificationType.FORUM_REPLY
        }
    }
}

enum class NotificationType {
    FORUM_REPLY,        // Balasan langsung ke post
    NESTED_REPLY,       // Balasan ke balasan
    POST_UPDATE         // Update pada post (opsional untuk fitur masa depan)
}

data class NotificationCount(
    val unreadCount: Int = 0
)
package com.example.banksampah.data

data class User(
    var uid: String = "",
    var fullName: String = "",
    var displayName: String = "",
    var email: String = "",
    var profilePhotoUrl: String = "",
    var role: UserRole = UserRole.USER,
    var isAdmin: Boolean = false,
    var createdAt: Long = 0,
    // Data dari UserProfile
    var address: String = "",
    var phoneNumber: String = "",
    var nik: String = ""
) {
    // Helper function untuk backward compatibility
    fun getRoleType(): UserRole {
        return if (isAdmin && role == UserRole.USER) {
            UserRole.ADMIN
        } else {
            role
        }
    }

    // Check permissions
    fun isKaderOrAdmin(): Boolean = role == UserRole.KADER || role == UserRole.ADMIN
    fun canManageContent(): Boolean = isKaderOrAdmin()
    fun canManageUsers(): Boolean = role == UserRole.ADMIN
}

enum class UserRole(val displayName: String, val level: Int) {
    USER("User Biasa", 0),
    KADER("Kader", 1),
    ADMIN("Admin", 2);

    companion object {
        fun fromString(value: String): UserRole {
            return values().find { it.name == value } ?: USER
        }
    }
}
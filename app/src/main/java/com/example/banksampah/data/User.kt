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
    // ✅ Helper untuk Firebase - convert role ke string
    fun getRoleString(): String = role.name

    // ✅ Helper untuk set role dari string (dari Firebase)
    fun setRoleFromString(roleString: String) {
        role = try {
            UserRole.valueOf(roleString)
        } catch (e: Exception) {
            UserRole.USER
        }
    }

    // ✅ Helper function dengan backward compatibility
    fun getRoleType(): UserRole {
        // Jika isAdmin true tapi role masih USER, upgrade ke ADMIN
        return if (isAdmin && role == UserRole.USER) {
            UserRole.ADMIN
        } else {
            role
        }
    }

    // Check permissions
    fun isKaderOrAdmin(): Boolean = getRoleType() == UserRole.KADER || getRoleType() == UserRole.ADMIN
    fun canManageContent(): Boolean = isKaderOrAdmin()
    fun canManageUsers(): Boolean = getRoleType() == UserRole.ADMIN
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
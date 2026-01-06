package com.example.banksampah.data

// Data model untuk Album Kegiatan
data class ActivityAlbum(
    var id: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "", // Format: "01 Januari 2024"
    val location: String = "",
    val coverImageUrl: String = "", // Foto cover album
    val timestamp: Long = System.currentTimeMillis(),
    val photoCount: Int = 0 // Jumlah foto dalam album
)

// Data model untuk Foto dalam Album
data class AlbumPhoto(
    var id: String = "",
    val albumId: String = "",
    val imageUrl: String = "",
    val caption: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val order: Int = 0 // Urutan foto dalam album
)
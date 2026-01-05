package com.example.banksampah.data

// Data model untuk artikel edukasi
data class EdukasiItem(
    var id: String = "",
    val title: String = "",
    val imageUrl: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val order: Int = 0 // Untuk urutan tampilan
)

// Data model untuk galeri Bank Sampah
data class GalleryItem(
    var id: String = "",
    val imageUrl: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

// Edukasi default "Tentang Bank Sampah"
object EdukasiConstants {
    const val TENTANG_BANK_SAMPAH_ID = "tentang_bank_sampah"
    const val TENTANG_BANK_SAMPAH_TITLE = "Tentang Bank Sampah"
}

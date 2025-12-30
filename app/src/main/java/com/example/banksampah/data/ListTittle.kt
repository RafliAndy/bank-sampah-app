package com.example.banksampah.data

import com.example.banksampah.R

data class ListTittle(
    val id: Int,
    val tittleImg: Int,
    val tittleText: String
)

val dummyListTittle = listOf(
    ListTittle(1, R.drawable.image1, "Apa Dampak Sampah terhadap Kesehatan?"),
    ListTittle(2, R.drawable.image2, "Apa Jenis-Jenis Dari Sampah?"),
    ListTittle(3, R.drawable.image3, "Bagaimana Cara Pengelolaan Sampah yang Benar?"),
    ListTittle(4, R.drawable.image4, "Apa Itu Bank Sampah dan Manfaat ekonominya Bagi Masyarakat?"),
    ListTittle(5, R.drawable.image5, "Apa Inovasi dalam Mengelola Sampah Ramah Lingkungan?"),
    ListTittle(6, R.drawable.image6, "Bagaimana Peraturan dan Tanggung Jawab Kita Dalam Menjaga Lingkungan?"),
    ListTittle(7, R.drawable.image7, "Tentang Bank Sampah"),
    )
package com.example.banksampah.data

import com.example.banksampah.R


data class NewsItems(
    val id: Int,
    val title: String,
    val date: String,
    val content: String,
    val imageRes: Int
)


    val dummyNewsItems = listOf(
        NewsItems(
            id = 1,
            title = "Gunungan Sampah di Pasar Cikurubuk Tasik Ganggu Kenyamanan",
            date = "10/10/2023",
            content = "Pengunjung Pasar Cikurubuk Tasikmalaya mengeluhkan tumpukan sampah...",
            imageRes = R.drawable.news1
        ),
        NewsItems(
            id = 2,
            title = "Bank Sampah di Jakarta Capai Rekor Pengolahan",
            date = "10/12/2023",
            content = "Bank sampah di Jakarta berhasil mengolah 10 ton sampah...",
            imageRes = R.drawable.news2
        )

    )

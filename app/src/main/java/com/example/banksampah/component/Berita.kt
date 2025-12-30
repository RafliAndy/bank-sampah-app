package com.example.banksampah.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.banksampah.R
import com.example.banksampah.data.NewsItems
import com.example.banksampah.ui.theme.BankSampahTheme

@Composable
fun Berita(newsItems: NewsItems) {
    Card (
        modifier = Modifier
            .width(280.dp)
            .height(200.dp)
            .clickable { /* Handle click */ },
        shape = RoundedCornerShape(8.dp),
    ){
        Box(
            modifier = Modifier
                .height(120.dp)
                .fillMaxWidth()
                .padding(2.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            Image(
                painter = painterResource(id = newsItems.imageRes),
                contentDescription = "Gambar Berita",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Column (
            modifier = Modifier.padding(8.dp),
        ) {
            Text(
                text = newsItems.date,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                modifier = Modifier
            )
            Text(
                text = newsItems.title,
                maxLines = 2,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview
@Composable
fun BeritaPreview() {
    BankSampahTheme {
        Berita(newsItems = NewsItems(1, "Gunungan Sampah di Pasar Cikurubuk Tasik Ganggu Kenyamanan", "10/12/2023", "Pengunjung Pasar Cikurubuk Tasikmalaya mengeluhkan tumpukan sampah...", R.drawable.news1))
    }
}
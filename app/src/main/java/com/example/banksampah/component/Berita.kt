package com.example.banksampah.component

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.banksampah.data.NewsItem

@Composable
fun Berita(news: NewsItem) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .width(280.dp)
            .height(220.dp)
            .clickable {
                // Buka link berita di browser
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(news.link))
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Gunakan SubcomposeAsyncImage untuk loading & error handling
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(news.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = news.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                loading = {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(130.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                },
                error = {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(130.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📰",
                            style = MaterialTheme.typography.displayMedium
                        )
                    }
                }
            )

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = news.date,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = news.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
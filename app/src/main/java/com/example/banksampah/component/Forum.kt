package com.example.banksampah.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.banksampah.R
import com.example.banksampah.Routes
import com.example.banksampah.data.ForumPost
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

@Composable
fun ForumList(navController: NavHostController) {
    val forumPosts = remember { mutableStateOf<List<ForumPost>>(emptyList()) }

    // Load data dari Firebase
    LaunchedEffect(Unit) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("posts")
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val posts = mutableListOf<ForumPost>()
                for (postSnapshot in snapshot.children) {
                    val post = postSnapshot.getValue(ForumPost::class.java)
                    post?.let {
                        // Pastikan ID ada
                        if (it.id.isEmpty()) {
                            it.id = postSnapshot.key ?: UUID.randomUUID().toString()
                        }
                        posts.add(it)
                    }
                }
                // Urutkan berdasarkan timestamp (terbaru pertama)
                forumPosts.value = posts.sortedByDescending { it.timestamp }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // PERBAIKAN: Gunakan LazyColumn dengan constraint yang benar
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(forumPosts.value) { post ->
                ForumItem(post = post, navController = navController)
            }
        }
    }
}

@Composable
fun CloudinaryImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    contentDescription: String = "Post image"
) {
    if (imageUrl.isNullOrEmpty()) {
        // Tidak menampilkan apa-apa jika tidak ada gambar
        return
    }

    var showError by remember { mutableStateOf(false) }

    if (showError) {
        // Fallback jika gambar gagal dimuat
        Box(
            modifier = modifier
                .background(Color.LightGray)
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Gagal memuat gambar",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    } else {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            modifier = modifier
                .clip(RoundedCornerShape(8.dp)),
            onError = {
                showError = true
            }
        )
    }
}

@Composable
fun ForumItem(post: ForumPost, navController: NavHostController) {

    var replyCount by remember { mutableStateOf(0) }

    LaunchedEffect(post.id) {
        val repliesRef = FirebaseDatabase.getInstance().getReference("replies")

        repliesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var count = 0
                for (replySnap in snapshot.children) {
                    val postId = replySnap.child("postId").getValue(String::class.java)
                    if (postId == post.id) {
                        count++
                    }
                }
                replyCount = count
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

        // Debug: print URL gambar ke log
    println("DEBUG - Post ID: ${post.id}, Image URL: ${post.imageUrl}")

    Column(
        modifier = Modifier
            .background(colorResource(id = R.color.greenlight))
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(35.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Person Icon",
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center),
                    tint = Color.Black
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = post.authorName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatTimeAgo(post.timestamp),
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column {
            Text(
                text = post.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = post.body,
                fontSize = 14.sp
            )

            // Penggunaan di ForumItem
            post.imageUrl?.let { imageUrl ->
                Spacer(modifier = Modifier.height(12.dp))

                CloudinaryImage(
                    imageUrl = imageUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    // Navigasi ke halaman detail menggunakan helper function
                    navController.navigate(Routes.forumDetail(post.id))
                },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.green)
                ),
                modifier = Modifier.width(150.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Reply Icon",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Balas",
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                colorResource(id = R.color.greenlight),
                                RoundedCornerShape(100)
                            )
                    ) {
                        Text(
                            text = replyCount.toString(), // Nanti bisa diganti dengan jumlah balasan dari Firebase
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

        }
    }
}

// Fungsi untuk format waktu
fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> "$days hari yang lalu"
        hours > 0 -> "$hours jam yang lalu"
        minutes > 0 -> "$minutes menit yang lalu"
        else -> "Baru saja"
    }
}


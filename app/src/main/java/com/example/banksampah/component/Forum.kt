package com.example.banksampah.component

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.banksampah.R
import com.example.banksampah.Routes
import com.example.banksampah.data.ForumCategory
import com.example.banksampah.data.ForumPost
import com.google.firebase.database.*
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ForumList(navController: NavHostController) {
    val forumPosts = remember { mutableStateOf<List<ForumPost>>(emptyList()) }

    // ✅ State untuk Search & Filter
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ForumCategory?>(null) }

    // Load data dari Firebase
    LaunchedEffect(Unit) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("posts")
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val posts = mutableListOf<ForumPost>()
                for (postSnapshot in snapshot.children) {
                    val post = postSnapshot.getValue(ForumPost::class.java)
                    post?.let {
                        if (it.id.isEmpty()) {
                            it.id = postSnapshot.key ?: UUID.randomUUID().toString()
                        }
                        posts.add(it)
                    }
                }
                forumPosts.value = posts.sortedByDescending { it.timestamp }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // ✅ Filter posts berdasarkan search dan kategori
    val filteredPosts = remember(forumPosts.value, searchQuery, selectedCategory) {
        forumPosts.value.filter { post ->
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                post.title.contains(searchQuery, ignoreCase = true) ||
                        post.body.contains(searchQuery, ignoreCase = true) ||
                        post.authorName.contains(searchQuery, ignoreCase = true)
            }

            val matchesCategory = selectedCategory?.let { category ->
                post.category == category.name
            } ?: true

            matchesSearch && matchesCategory
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // ✅ Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Cari post, judul, atau pengguna...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = colorResource(id = R.color.green))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(id = R.color.green),
                unfocusedBorderColor = Color.Gray
            ),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        // ✅ Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            // Chip "Semua"
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("Semua") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colorResource(id = R.color.green),
                        selectedLabelColor = Color.White
                    )
                )
            }

            // Chips untuk setiap kategori
            items(ForumCategory.getAllCategories()) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = {
                        selectedCategory = if (selectedCategory == category) null else category
                    },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(category.color), RoundedCornerShape(50))
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(category.displayName)
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(category.color),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ✅ Info hasil filter
        if (searchQuery.isNotEmpty() || selectedCategory != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${filteredPosts.size} hasil ditemukan",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                if (searchQuery.isNotEmpty() || selectedCategory != null) {
                    TextButton(onClick = {
                        searchQuery = ""
                        selectedCategory = null
                    }) {
                        Text("Reset Filter", fontSize = 12.sp)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // ✅ List Posts
        if (filteredPosts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        if (searchQuery.isNotEmpty() || selectedCategory != null)
                            "Tidak ada hasil yang cocok"
                        else
                            "Belum ada postingan",
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredPosts) { post ->
                    ForumItem(post = post, navController = navController)
                }
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
        return
    }

    var showError by remember { mutableStateOf(false) }

    if (showError) {
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

    // Ambil kategori
    val category = ForumCategory.fromString(post.category)

    LaunchedEffect(post.id) {
        val repliesRef = FirebaseDatabase.getInstance().getReference("replies")
        repliesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var count = 0
                for (replySnap in snapshot.children) {
                    val postId = replySnap.child("postId").getValue(String::class.java)
                    if (postId == post.id) count++
                }
                replyCount = count
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Column(
        modifier = Modifier
            .background(colorResource(id = R.color.greenlight))
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // ✅ Kategori Tag
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(category.color),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    category.displayName,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Text(
                text = formatTimeAgo(post.timestamp),
                fontSize = 10.sp,
                color = Color.Gray
            )
        }

        Spacer(Modifier.height(12.dp))

        // Header dengan foto profil dan nama
        Row(verticalAlignment = Alignment.CenterVertically) {
            UserProfileImage(uid = post.uid, size = 40.dp, showAdminBadge = true)
            Spacer(Modifier.width(8.dp))
            Column {
                UserNameWithBadge(uid = post.uid, authorName = post.authorName, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(formatTimeAgo(post.timestamp), fontSize = 10.sp, color = Color.Gray)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Konten post
        Column {
            Text(post.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                text = post.body,
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable { navController.navigate(Routes.forumDetail(post.id)) }
            )

            if (post.body.length > 120) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "(selengkapnya)",
                    fontSize = 12.sp,
                    color = colorResource(id = R.color.green),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { navController.navigate(Routes.forumDetail(post.id)) }
                )
            }

            post.imageUrl?.let { imageUrl ->
                Spacer(Modifier.height(12.dp))
                CloudinaryImage(imageUrl = imageUrl, modifier = Modifier.fillMaxWidth().height(200.dp))
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { navController.navigate(Routes.forumDetail(post.id)) },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.green)),
                modifier = Modifier.width(150.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Comment, contentDescription = "Reply Icon", tint = Color.Black, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Balas", fontSize = 14.sp, color = Color.Black)
                    Spacer(Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(colorResource(id = R.color.greenlight), RoundedCornerShape(100))
                    ) {
                        Text(
                            text = replyCount.toString(),
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
    val diffMillis = now - timestamp

    val seconds = diffMillis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    val months = days / 30
    val years = months / 12

    return when {
        years > 0 -> {
            if (years == 1L) "1 tahun yang lalu"
            else "$years tahun yang lalu"
        }
        months > 0 -> {
            if (months == 1L) "1 bulan yang lalu"
            else "$months bulan yang lalu"
        }
        days > 0 -> {
            if (days == 1L) "1 hari yang lalu"
            else "$days hari yang lalu"
        }
        hours > 0 -> {
            if (hours == 1L) "1 jam yang lalu"
            else "$hours jam yang lalu"
        }
        minutes > 0 -> {
            if (minutes == 1L) "1 menit yang lalu"
            else "$minutes menit yang lalu"
        }
        else -> "Baru saja"
    }
}
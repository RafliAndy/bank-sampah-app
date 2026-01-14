package com.example.banksampah.component

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.banksampah.R
import com.example.banksampah.Routes
import com.example.banksampah.data.ForumPost
import com.example.banksampah.viewmodel.ForumViewModel

@Composable
fun UserForumListSection(navController: NavHostController) {
    val viewModel: ForumViewModel = viewModel()
    val postsState by viewModel.postsState.collectAsState()
    val deleteState by viewModel.deleteState.collectAsState()
    val context = LocalContext.current

    // Load posts when component is first composed
    LaunchedEffect(Unit) {
        viewModel.loadMyPosts()
    }

    // Handle delete result
    LaunchedEffect(deleteState) {
        when (val state = deleteState) {
            is ForumViewModel.DeleteState.Success -> {
                Toast.makeText(context, "Postingan berhasil dihapus", Toast.LENGTH_SHORT).show()
                viewModel.resetDeleteState()
            }
            is ForumViewModel.DeleteState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                viewModel.resetDeleteState()
            }
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Postingan Saya",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.green)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content based on state
        when (val state = postsState) {
            is ForumViewModel.PostsState.Loading -> {
                LoadingSection()
            }

            is ForumViewModel.PostsState.Success -> {
                if (state.posts.isEmpty()) {
                    EmptySection()
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.posts) { post ->
                            ForumPostCard(
                                post = post,
                                onPostClick = {
                                    navController.navigate(Routes.forumDetail(post.id))
                                },
                                onDeleteClick = {
                                    viewModel.deletePost(post.id)
                                },
                                isDeleting = deleteState is ForumViewModel.DeleteState.Deleting
                            )
                        }
                    }
                }
            }

            is ForumViewModel.PostsState.Empty -> {
                EmptySection(message = state.message)
            }

            is ForumViewModel.PostsState.Error -> {
                ErrorSection(
                    message = state.message,
                    onRetry = { viewModel.loadMyPosts() }
                )
            }
        }
    }
}

@Composable
private fun ForumPostCard(
    post: ForumPost,
    onPostClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isDeleting: Boolean
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPostClick() },
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.greenlight)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header with delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = post.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { showDeleteDialog = true },
                    enabled = !isDeleting,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body preview
            Text(
                text = post.body,
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Footer info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTimeAgo(post.timestamp),
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                if (post.imageUrl?.isNotEmpty() == true) {
                    Text(
                        text = "📷 Gambar",
                        fontSize = 12.sp,
                        color = colorResource(id = R.color.green)
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Postingan") },
            text = { Text("Apakah Anda yakin ingin menghapus postingan ini? Semua balasan juga akan dihapus.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Red
                    )
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun LoadingSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = colorResource(id = R.color.green)
        )
    }
}

@Composable
private fun EmptySection(message: String = "Anda belum membuat postingan") {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📝",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun ErrorSection(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFEBEE)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "❌",
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color.Red
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.green)
                )
            ) {
                Text("Coba Lagi")
            }
        }
    }
}
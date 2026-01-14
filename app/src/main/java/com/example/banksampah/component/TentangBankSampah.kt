package com.example.banksampah

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import com.example.banksampah.data.ActivityAlbum
import com.example.banksampah.viewmodel.AlbumViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TentangBankSampahScreen(navController: NavHostController) {
    val albumViewModel: AlbumViewModel = viewModel()
    val albumsState by albumViewModel.albumsState.collectAsState()
    var isAdmin by remember { mutableStateOf(false) }

    // Check admin status
    LaunchedEffect(Unit) {
        albumViewModel.loadAlbums()
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.uid?.let { uid ->
            val adminRef = FirebaseDatabase.getInstance().getReference("users/$uid/isAdmin")
            adminRef.get().addOnSuccessListener { snapshot ->
                isAdmin = snapshot.getValue(Boolean::class.java) ?: false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bank Sampah Rawa Panjang") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.green),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "background",
                modifier = Modifier.fillMaxSize()
            )

            when (val state = albumsState) {
                is AlbumViewModel.AlbumsState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colorResource(id = R.color.green))
                    }
                }

                is AlbumViewModel.AlbumsState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Admin button
                        if (isAdmin) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Button(
                                    onClick = { navController.navigate(Routes.ADMIN_ALBUMS) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    colorResource(id = R.color.darkgreen),
                                                    colorResource(id = R.color.green)
                                                )
                                            ),
                                            shape = RoundedCornerShape(24.dp)
                                        )
                                        .shadow(
                                            elevation = 4.dp,
                                            shape = RoundedCornerShape(24.dp)
                                        )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PhotoAlbum,
                                            contentDescription = "Kelola Album",
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            text = "Kelola Album Kegiatan",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Albums list
                        if (state.albums.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Belum ada album kegiatan",
                                        fontSize = 16.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(state.albums) { album ->
                                    AlbumCard(
                                        album = album,
                                        onClick = {
                                            navController.navigate(Routes.albumDetail(album.id))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                is AlbumViewModel.AlbumsState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = state.message,
                                color = Color.Red,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { albumViewModel.loadAlbums() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorResource(id = R.color.green)
                                )
                            ) {
                                Text("Coba Lagi")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlbumCard(
    album: ActivityAlbum,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column {
            // Cover Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                if (album.coverImageUrl.isNotEmpty()) {
                    SubcomposeAsyncImage(
                        model = album.coverImageUrl,
                        contentDescription = album.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = Color.Gray
                                )
                            }
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.Gray
                        )
                    }
                }

                // Photo count badge
                if (album.photoCount > 0) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Photo,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Text(
                                text = "${album.photoCount}",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Album Info
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = album.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Date and Location
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (album.date.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = colorResource(id = R.color.green)
                            )
                            Text(
                                text = album.date,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    if (album.location.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = colorResource(id = R.color.green)
                            )
                            Text(
                                text = album.location,
                                fontSize = 12.sp,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (album.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = album.description,
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
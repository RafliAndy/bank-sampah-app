package com.example.banksampah

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import com.example.banksampah.data.ActivityAlbum
import com.example.banksampah.viewmodel.AlbumViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAlbumsScreen(navController: NavHostController) {
    val albumViewModel: AlbumViewModel = viewModel()
    val albumsState by albumViewModel.albumsState.collectAsState()
    val operationState by albumViewModel.operationState.collectAsState()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var editingAlbum by remember { mutableStateOf<ActivityAlbum?>(null) }
    var deletingAlbum by remember { mutableStateOf<ActivityAlbum?>(null) }

    // Handle operation result
    LaunchedEffect(operationState) {
        when (val state = operationState) {
            is AlbumViewModel.OperationState.Success -> {
                Toast.makeText(context, "Berhasil!", Toast.LENGTH_SHORT).show()
                albumViewModel.resetOperationState()
                showAddDialog = false
                editingAlbum = null
                deletingAlbum = null
            }
            is AlbumViewModel.OperationState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                albumViewModel.resetOperationState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelola Album") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = Color.White
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
                    if (state.albums.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text("Belum ada album")
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { showAddDialog = true }) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Tambah Album")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.albums) { album ->
                                AdminAlbumCard(
                                    album = album,
                                    onEdit = { editingAlbum = album },
                                    onDelete = { deletingAlbum = album },
                                    onManagePhotos = {
                                        navController.navigate(Routes.adminAlbumPhotos(album.id))
                                    }
                                )
                            }
                        }
                    }
                }

                is AlbumViewModel.AlbumsState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.message, color = Color.Red)
                            Button(onClick = { albumViewModel.loadAlbums() }) {
                                Text("Coba Lagi")
                            }
                        }
                    }
                }
            }
        }
    }

    // Add/Edit Dialog
    if (showAddDialog || editingAlbum != null) {
        AlbumFormDialog(
            album = editingAlbum,
            onDismiss = {
                showAddDialog = false
                editingAlbum = null
            },
            onSave = { title, description, date, location, coverUri ->
                if (editingAlbum != null) {
                    albumViewModel.updateAlbum(
                        editingAlbum!!.copy(
                            title = title,
                            description = description,
                            date = date,
                            location = location
                        ),
                        coverUri
                    )
                } else {
                    albumViewModel.addAlbum(title, description, date, location, coverUri)
                }
            },
            isLoading = operationState is AlbumViewModel.OperationState.Loading
        )
    }

    // Delete Dialog
    deletingAlbum?.let { album ->
        AlertDialog(
            onDismissRequest = { deletingAlbum = null },
            title = { Text("Hapus Album") },
            text = { Text("Apakah Anda yakin? Semua foto dalam album juga akan terhapus.") },
            confirmButton = {
                TextButton(
                    onClick = { albumViewModel.deleteAlbum(album.id) },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingAlbum = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun AdminAlbumCard(
    album: ActivityAlbum,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onManagePhotos: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cover Image
            if (album.coverImageUrl.isNotEmpty()) {
                SubcomposeAsyncImage(
                    model = album.coverImageUrl,
                    contentDescription = album.title,
                    modifier = Modifier.size(80.dp),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                )
            } else {
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = album.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${album.photoCount} foto",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row {
                    IconButton(onClick = onManagePhotos) {
                        Icon(
                            imageVector = Icons.Default.Photo,
                            contentDescription = "Kelola Foto",
                            tint = colorResource(id = R.color.green)
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = colorResource(id = R.color.green)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AlbumFormDialog(
    album: ActivityAlbum?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, Uri?) -> Unit,
    isLoading: Boolean
) {
    var title by remember { mutableStateOf(album?.title ?: "") }
    var description by remember { mutableStateOf(album?.description ?: "") }
    var date by remember { mutableStateOf(album?.date ?: "") }
    var location by remember { mutableStateOf(album?.location ?: "") }
    var coverUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        coverUri = uri
    }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text(if (album == null) "Tambah Album" else "Edit Album") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Kegiatan") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    enabled = !isLoading
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Tanggal (contoh: 01 Januari 2024)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Lokasi") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading
                )

                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (coverUri != null) "Foto Cover Dipilih" else "Pilih Foto Cover")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(title, description, date, location, coverUri)
                },
                enabled = !isLoading && title.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Simpan")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Batal")
            }
        }
    )
}
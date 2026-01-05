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
import com.example.banksampah.data.EdukasiItem
import com.example.banksampah.viewmodel.EdukasiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEdukasiScreen(navController: NavHostController) {
    val edukasiViewModel: EdukasiViewModel = viewModel()
    val edukasiState by edukasiViewModel.edukasiState.collectAsState()
    val operationState by edukasiViewModel.operationState.collectAsState()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var editingEdukasi by remember { mutableStateOf<EdukasiItem?>(null) }
    var deletingEdukasi by remember { mutableStateOf<EdukasiItem?>(null) }

    // Handle operation result
    LaunchedEffect(operationState) {
        when (val state = operationState) {
            is EdukasiViewModel.OperationState.Success -> {
                Toast.makeText(context, "Berhasil!", Toast.LENGTH_SHORT).show()
                edukasiViewModel.resetOperationState()
                showAddDialog = false
                editingEdukasi = null
                deletingEdukasi = null
            }
            is EdukasiViewModel.OperationState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                edukasiViewModel.resetOperationState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelola Edukasi") },
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

            when (val state = edukasiState) {
                is EdukasiViewModel.EdukasiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colorResource(id = R.color.green))
                    }
                }

                is EdukasiViewModel.EdukasiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.edukasiList) { edukasi ->
                            AdminEdukasiCard(
                                edukasi = edukasi,
                                onEdit = { editingEdukasi = edukasi },
                                onDelete = { deletingEdukasi = edukasi }
                            )
                        }
                    }
                }

                is EdukasiViewModel.EdukasiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(text = state.message, color = Color.Red)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { edukasiViewModel.loadEdukasi() }) {
                                Text("Coba Lagi")
                            }
                        }
                    }
                }
            }
        }
    }

    // Add/Edit Dialog
    if (showAddDialog || editingEdukasi != null) {
        EdukasiFormDialog(
            edukasi = editingEdukasi,
            onDismiss = {
                showAddDialog = false
                editingEdukasi = null
            },
            onSave = { title, content, order, imageUri ->
                if (editingEdukasi != null) {
                    edukasiViewModel.updateEdukasi(
                        editingEdukasi!!.copy(
                            title = title,
                            content = content,
                            order = order
                        ),
                        imageUri
                    )
                } else {
                    edukasiViewModel.addEdukasi(
                        EdukasiItem(
                            title = title,
                            content = content,
                            order = order
                        ),
                        imageUri
                    )
                }
            },
            isLoading = operationState is EdukasiViewModel.OperationState.Loading
        )
    }

    // Delete Confirmation Dialog
    deletingEdukasi?.let { edukasi ->
        AlertDialog(
            onDismissRequest = { deletingEdukasi = null },
            title = { Text("Hapus Edukasi") },
            text = { Text("Apakah Anda yakin ingin menghapus \"${edukasi.title}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        edukasiViewModel.deleteEdukasi(edukasi.id)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingEdukasi = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun AdminEdukasiCard(
    edukasi: EdukasiItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
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
            // Image
            if (edukasi.imageUrl.isNotEmpty()) {
                SubcomposeAsyncImage(
                    model = edukasi.imageUrl,
                    contentDescription = edukasi.title,
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
                    modifier = Modifier
                        .size(80.dp)
                        .padding(8.dp),
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
                    text = edukasi.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Urutan: ${edukasi.order}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Column {
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

@Composable
fun EdukasiFormDialog(
    edukasi: EdukasiItem?,
    onDismiss: () -> Unit,
    onSave: (String, String, Int, Uri?) -> Unit,
    isLoading: Boolean
) {
    var title by remember { mutableStateOf(edukasi?.title ?: "") }
    var content by remember { mutableStateOf(edukasi?.content ?: "") }
    var order by remember { mutableStateOf(edukasi?.order?.toString() ?: "0") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text(if (edukasi == null) "Tambah Edukasi" else "Edit Edukasi") },
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
                    label = { Text("Judul") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Konten") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    maxLines = 10,
                    enabled = !isLoading
                )

                OutlinedTextField(
                    value = order,
                    onValueChange = { order = it.filter { char -> char.isDigit() } },
                    label = { Text("Urutan (angka)") },
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
                    Text(if (imageUri != null) "Gambar Dipilih" else "Pilih Gambar")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(title, content, order.toIntOrNull() ?: 0, imageUri)
                },
                enabled = !isLoading && title.isNotBlank() && content.isNotBlank()
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
package com.example.banksampah

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import coil.request.ImageRequest
import com.example.banksampah.component.MainTopBar
import com.example.banksampah.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavHostController) {
    val profileViewModel: ProfileViewModel = viewModel()
    val profileState by profileViewModel.profileState.collectAsState()
    val updateState by profileViewModel.updateState.collectAsState()
    val context = LocalContext.current

    var displayName by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
        }
    }

    // Load user data saat pertama kali
    LaunchedEffect(profileState) {
        if (profileState is ProfileViewModel.ProfileState.Success) {
            val user = (profileState as ProfileViewModel.ProfileState.Success).user
            displayName = user.displayName.ifEmpty { user.fullName }
        }
    }

    // Handle update result
    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is ProfileViewModel.UpdateState.Success -> {
                Toast.makeText(context, "Profil berhasil diupdate!", Toast.LENGTH_SHORT).show()
                profileViewModel.resetUpdateState()
                navController.popBackStack()
            }
            is ProfileViewModel.UpdateState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                profileViewModel.resetUpdateState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
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

            when (val state = profileState) {
                is ProfileViewModel.ProfileState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is ProfileViewModel.ProfileState.Success -> {
                    val user = state.user

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Profile Photo Section
                        Box(
                            modifier = Modifier
                                .size(150.dp)
                                .clip(CircleShape)
                                .border(
                                    BorderStroke(4.dp, colorResource(id = R.color.green)),
                                    CircleShape
                                )
                                .background(Color.LightGray)
                                .clickable { imagePickerLauncher.launch("image/*") }
                        ) {
                            // Show current or selected image
                            val imageUrl = selectedImageUri?.toString() ?: user.profilePhotoUrl

                            if (imageUrl.isNotEmpty()) {
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Profile Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                    loading = {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    },
                                    error = {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Default",
                                            modifier = Modifier
                                                .size(80.dp)
                                                .align(Alignment.Center),
                                            tint = Color.White
                                        )
                                    }
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Default Profile",
                                    modifier = Modifier
                                        .size(80.dp)
                                        .align(Alignment.Center),
                                    tint = Color.White
                                )
                            }

                            // Camera overlay icon
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(CircleShape)
                                    .background(colorResource(id = R.color.green))
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Change Photo",
                                    tint = Color.White,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Tap untuk ganti foto",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        // Delete Photo Button (if has photo)
                        if (user.profilePhotoUrl.isNotEmpty() && selectedImageUri == null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { profileViewModel.deleteProfilePhoto() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.Red,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Hapus Foto",
                                    color = Color.Red,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Display Name Section
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.9f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Nama Tampilan",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorResource(id = R.color.green)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = displayName,
                                    onValueChange = { displayName = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Masukkan nama tampilan") },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colorResource(id = R.color.green),
                                        unfocusedBorderColor = Color.Gray
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Nama asli: ${user.fullName}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Save Button
                        Button(
                            onClick = {
                                // Upload foto jika ada yang dipilih
                                if (selectedImageUri != null) {
                                    profileViewModel.updateProfilePhoto(selectedImageUri!!)
                                }

                                // Update display name jika berubah
                                if (displayName != user.displayName && displayName.isNotEmpty()) {
                                    profileViewModel.updateDisplayName(displayName)
                                }

                                // Jika tidak ada perubahan
                                if (selectedImageUri == null && displayName == user.displayName) {
                                    Toast.makeText(
                                        context,
                                        "Tidak ada perubahan",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.popBackStack()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = updateState !is ProfileViewModel.UpdateState.Updating,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorResource(id = R.color.green)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (updateState is ProfileViewModel.UpdateState.Updating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    text = "Simpan Perubahan",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                is ProfileViewModel.ProfileState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = state.message, color = Color.Red)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { profileViewModel.loadProfile() }) {
                                Text("Coba Lagi")
                            }
                        }
                    }
                }
            }
        }
    }
}
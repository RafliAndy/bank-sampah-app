package com.example.banksampah.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import com.example.banksampah.R
import com.example.banksampah.Routes
import com.example.banksampah.data.EdukasiConstants
import com.example.banksampah.data.EdukasiItem
import com.example.banksampah.viewmodel.EdukasiViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@Composable
fun MainEdukasiSection(navController: NavHostController) {
    val edukasiViewModel: EdukasiViewModel = viewModel()
    val edukasiState by edukasiViewModel.edukasiState.collectAsState()
    var isAdmin by remember { mutableStateOf(false) }

    // Check admin status
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.uid?.let { uid ->
            val adminRef = FirebaseDatabase.getInstance().getReference("users/$uid/isAdmin")
            adminRef.get().addOnSuccessListener { snapshot ->
                isAdmin = snapshot.getValue(Boolean::class.java) ?: false
            }
        }
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Ketahuilah!!!",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            if (isAdmin) {
                TextButton(
                    onClick = { navController.navigate(Routes.ADMIN_EDUKASI) }
                ) {
                    Text(
                        "Kelola",
                        color = colorResource(id = R.color.green),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        when (val state = edukasiState) {
            is EdukasiViewModel.EdukasiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colorResource(id = R.color.green))
                }
            }

            is EdukasiViewModel.EdukasiState.Success -> {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Regular edukasi items
                    state.edukasiList.forEach { edukasi ->
                        EdukasiCard(
                            edukasi = edukasi,
                            onClick = {
                                navController.navigate(Routes.edukasiDetail(edukasi.id))
                            }
                        )
                    }

                    // Special "Tentang Bank Sampah" item (always last)
                    TentangBankSampahCard(
                        onClick = {
                            navController.navigate(Routes.TENTANG_BANK_SAMPAH)
                        }
                    )
                }
            }

            is EdukasiViewModel.EdukasiState.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = Color.Red,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { edukasiViewModel.loadEdukasi() }) {
                            Text("Coba Lagi")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EdukasiCard(
    edukasi: EdukasiItem,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(id = R.color.greenlight),
            contentColor = Color.Black
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Image
            if (edukasi.imageUrl.isNotEmpty()) {
                SubcomposeAsyncImage(
                    model = edukasi.imageUrl,
                    contentDescription = edukasi.title,
                    modifier = Modifier
                        .size(61.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color.Black, RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(61.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                        .border(1.dp, Color.Black, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = Color.Gray
                    )
                }
            }

            // Title
            Text(
                text = edukasi.title,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Arrow
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow),
                contentDescription = "arrow",
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
fun TentangBankSampahCard(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(id = R.color.green),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Gallery icon
            Box(
                modifier = Modifier
                    .size(61.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(1.dp, Color.White, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = colorResource(id = R.color.green)
                )
            }

            // Title
            Text(
                text = EdukasiConstants.TENTANG_BANK_SAMPAH_TITLE,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = Color.White
            )

            // Arrow
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow),
                contentDescription = "arrow",
                modifier = Modifier.size(30.dp),
                tint = Color.White
            )
        }
    }
}
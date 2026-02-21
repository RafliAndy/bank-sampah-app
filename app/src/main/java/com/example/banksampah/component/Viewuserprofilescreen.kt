package com.example.banksampah

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.banksampah.data.User
import com.example.banksampah.data.UserRole
import com.example.banksampah.repository.ProfileRepository
import com.example.banksampah.viewmodel.GamificationViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewUserProfileScreen(
    navController: NavHostController,
    userId: String
) {
    val context = LocalContext.current
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val gamificationViewModel: GamificationViewModel = viewModel()

    // Load user data
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            val repository = ProfileRepository()
            val result = repository.getUserById(userId)

            if (result.isSuccess) {
                user = result.getOrThrow()
                isLoading = false
            } else {
                errorMessage = result.exceptionOrNull()?.message ?: "User tidak ditemukan"
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile Pengguna") },
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

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colorResource(id = R.color.green))
                    }
                }

                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = errorMessage ?: "Terjadi kesalahan",
                                color = Color.Red,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { navController.popBackStack() }) {
                                Text("Kembali")
                            }
                        }
                    }
                }

                user != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Profile Header
                        ProfileHeaderSection(user = user!!)

                        Spacer(modifier = Modifier.height(16.dp))

                        // User Info
                        UserInfoSection(user = user!!)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Gamification Stats
                        UserStatsSection(userId = userId, gamificationViewModel = gamificationViewModel)

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileHeaderSection(user: User) {
    val context = LocalContext.current
    val userRole = user.getRoleType()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                try {
                    Color(android.graphics.Color.parseColor(user.profileColor))
                } catch (e: Exception) {
                    colorResource(id = R.color.green)
                }
            )
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Profile Photo
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(5.dp, Color.White, CircleShape)
                    .background(Color.LightGray)
            ) {
                if (user.profilePhotoUrl.isNotEmpty()) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(user.profilePhotoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Name
            Text(
                user.displayName.ifEmpty { user.fullName },
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 24.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // Custom Title
            if (user.customTitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "✨ ${user.customTitle}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            // Role Badge
            if (userRole != UserRole.USER) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when (userRole) {
                        UserRole.ADMIN -> "👑 Admin"
                        UserRole.KADER -> "⭐ Kader"
                        else -> ""
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            when (userRole) {
                                UserRole.ADMIN -> Color(0xFFF44336)
                                UserRole.KADER -> Color(0xFFFF9800)
                                else -> Color.Gray
                            },
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun UserInfoSection(user: User) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Informasi Pengguna",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.green)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Email
            InfoRow(label = "Email", value = user.email)

            // Phone
            if (user.phoneNumber.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow(label = "No. Telp", value = user.phoneNumber)
            }

            // Address
            if (user.address.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow(label = "Alamat", value = user.address)
            }

            // Joined Date
            if (user.createdAt > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("id", "ID"))
                val joinedDate = dateFormat.format(java.util.Date(user.createdAt))
                InfoRow(label = "Bergabung", value = joinedDate)
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontWeight = FontWeight.SemiBold,
            color = Color.Gray,
            fontSize = 12.sp
        )
        Text(
            value,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

@Composable
fun UserStatsSection(userId: String, gamificationViewModel: GamificationViewModel) {
    var gamification by remember { mutableStateOf<com.example.banksampah.data.UserGamification?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        val repository = com.example.banksampah.repository.GamificationRepository()
        val result = repository.getUserGamification(userId)

        if (result.isSuccess) {
            gamification = result.getOrThrow()
            isLoading = false
        } else {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = colorResource(id = R.color.green))
        }
    } else if (gamification != null) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Statistik",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.green)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Level dan Points
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "Level",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            "${gamification!!.level}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(id = R.color.green)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "Poin",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            "${gamification!!.totalPoints}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(id = R.color.green)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Activity Stats
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatCard(emoji = "📝", label = "Post", value = gamification!!.postCount.toString())
                    StatCard(emoji = "💬", label = "Reply", value = gamification!!.replyCount.toString())
                    StatCard(emoji = "⭐", label = "Helpful", value = gamification!!.helpfulAnswerCount.toString())
                }

                // Badges
                if (gamification!!.badges.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Badge (${gamification!!.badges.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Tampilkan earned badges
                    val earnedBadges = com.example.banksampah.data.BadgeDefinitions.ALL_BADGES.filter {
                        gamification!!.badges.contains(it.id)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        earnedBadges.take(3).forEach { badge ->
                            Surface(
                                color = Color(0xFFFFF3E0),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(badge.icon, fontSize = 24.sp)
                                    Text(
                                        badge.name.take(8),
                                        fontSize = 8.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(emoji: String, label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(id = R.color.green)
        )
        Text(
            label,
            fontSize = 10.sp,
            color = Color.Gray
        )
    }
}
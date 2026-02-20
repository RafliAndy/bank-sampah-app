package com.example.banksampah

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.banksampah.component.BottomBar
import com.example.banksampah.component.UserForumListSection
import com.example.banksampah.viewmodel.AuthViewModel
import com.example.banksampah.viewmodel.ProfileViewModel
import com.example.banksampah.viewmodel.GamificationViewModel
import com.example.banksampah.data.Badge
import com.example.banksampah.data.BadgeDefinitions
import com.example.banksampah.component.AdminMenuSection
import com.example.banksampah.component.CustomTitleDialog
import com.example.banksampah.component.LevelPerksSection
import com.example.banksampah.component.ProfileColorDialog
import com.example.banksampah.data.UserRole


@Composable
fun MainProfileApp(navController: NavHostController, authViewModel: AuthViewModel) {
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthViewModel.AuthState.LoggedOut ->
                navController.navigate(Routes.MAIN_LOGIN)
            is AuthViewModel.AuthState.Error -> Toast.makeText(
                context,
                (authState.value as AuthViewModel.AuthState.Error).message,
                Toast.LENGTH_SHORT
            ).show()
            else -> Unit
        }
    }

    Scaffold(bottomBar = { BottomBar(navController = navController, authViewModel = authViewModel) }) { paddingValues ->
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                MainProfile(navController, authViewModel)
            }
        }
    }
}

@Composable
fun MainProfile(navController: NavHostController, authViewModel: AuthViewModel) {
    val profileViewModel: ProfileViewModel = viewModel()
    val gamificationViewModel: GamificationViewModel = viewModel()
    val profileState by profileViewModel.profileState.collectAsState()
    val gamificationState by gamificationViewModel.userGamification.collectAsState()
    val context = LocalContext.current

    var showCustomTitleDialog by remember { mutableStateOf(false) }
    var showProfileColorDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        gamificationViewModel.loadUserGamification()
    }

    Column {
        // Profile Header Section
        when (val state = profileState) {

            is ProfileViewModel.ProfileState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ProfileViewModel.ProfileState.Success -> {

                val user = state.user
                val userRole = user.getRoleType()
                val profileColor = user.profileColor

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            try {
                                Color(android.graphics.Color.parseColor(profileColor))
                            } catch (e: Exception) {
                                colorResource(id = R.color.green)
                            }
                        )
                        .padding(16.dp)
                ) {

                    Column {

                        // ===== TOP ROW =====
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Profile",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )

                            TextButton(
                                onClick = { authViewModel.signout() },
                                modifier = Modifier
                                    .background(Color.Red, RoundedCornerShape(50))
                            ) {
                                Text(
                                    "Sign Out",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // ===== PROFILE INFO ROW =====
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 8.dp)
                        ) {

                            // PHOTO
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

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                            )  {

                                Text(
                                    "Hello,",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 16.sp
                                )

                                Text(
                                    user.displayName.ifEmpty { user.fullName },
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 24.sp
                                )

                                // ===== CUSTOM TITLE =====
                                if (user.customTitle.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "✨ ${user.customTitle}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }

                                // ===== ROLE BADGE =====
                                if (userRole != UserRole.USER) {
                                    Spacer(modifier = Modifier.height(4.dp))
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

                            IconButton(
                                onClick = { navController.navigate(Routes.EDIT_PROFILE) },
                                modifier = Modifier
                                    .background(Color(0x80FFFFFF), RoundedCornerShape(100))
                                    .size(35.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }

            is ProfileViewModel.ProfileState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.message)
                }
            }
        }


        // ===== GAMIFICATION STATS SECTION =====
        when (val state = gamificationState) {
            is GamificationViewModel.GamificationState.Success -> {
                UserGamificationStatsSection(
                    data = state.data,
                    viewModel = gamificationViewModel
                )
            }
            is GamificationViewModel.GamificationState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colorResource(id = R.color.green))
                }
            }
            is GamificationViewModel.GamificationState.Error -> {
            }
        }

        when (val state = profileState) {
            is ProfileViewModel.ProfileState.Success -> {
                AdminMenuSection(
                    navController = navController,
                    userRole = state.user.getRoleType()
                )
            }
            else -> Unit
        }


        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { navController.navigate(Routes.LEADERBOARD) },
            colors = CardDefaults.cardColors(
                containerColor = colorResource(id = R.color.green)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🏆", fontSize = 32.sp)
                    Column {
                        Text(
                            "Lihat Leaderboard",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Top kontributor bulan ini",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

// ===== LEVEL PERKS SECTION =====
        LevelPerksSection(
            currentLevel = (gamificationState as? GamificationViewModel.GamificationState.Success)?.data?.level ?: 1,
            onPerkClick = { perk ->
                when (perk.id) {
                    "custom_title" -> {
                        // Check if level 7+
                        val currentLevel = (gamificationState as? GamificationViewModel.GamificationState.Success)?.data?.level ?: 1
                        if (currentLevel >= 7) {
                            showCustomTitleDialog = true
                        } else {
                            Toast.makeText(
                                context,
                                "Capai Level 7 untuk unlock fitur ini",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    "profile_color" -> {
                        // Check if level 3+
                        val currentLevel = (gamificationState as? GamificationViewModel.GamificationState.Success)?.data?.level ?: 1
                        if (currentLevel >= 3) {
                            showProfileColorDialog = true
                        } else {
                            Toast.makeText(
                                context,
                                "Capai Level 3 untuk unlock fitur ini",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        )



        // User Forum Posts Section
        UserForumListSection(navController = navController)
    }
    // Tambahkan dialogs
    if (showCustomTitleDialog) {
        CustomTitleDialog(
            currentTitle = (profileState as? ProfileViewModel.ProfileState.Success)?.user?.customTitle ?: "",
            onConfirm = { newTitle ->
                profileViewModel.updateCustomTitle(newTitle)
                showCustomTitleDialog = false
            },
            onDismiss = { showCustomTitleDialog = false }
        )
    }

    if (showProfileColorDialog) {
        ProfileColorDialog(
            currentColor = (profileState as? ProfileViewModel.ProfileState.Success)
                ?.user?.profileColor ?: "#4CAF50",
            onConfirm = { newColor ->
                profileViewModel.updateProfileColor(newColor)
                showProfileColorDialog = false
            },
            onDismiss = { showProfileColorDialog = false }
        )
    }

}

@Composable
fun UserGamificationStatsSection(
    data: com.example.banksampah.data.UserGamification,
    viewModel: GamificationViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header with Level & Points
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Level ${data.level}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(id = R.color.green)
                    )
                    Text(
                        text = "${data.totalPoints} poin",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Streak badge
                if (data.currentStreak > 0) {
                    Surface(
                        color = Color(0xFFFF9800),
                        shape = RoundedCornerShape(24.dp),
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("🔥", fontSize = 20.sp)
                            Text(
                                "${data.currentStreak} hari",
                                fontSize = 14.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Progress bar ke level berikutnya
            val progress = viewModel.getLevelProgress()
            val pointsToNext = viewModel.getPointsToNextLevel()

            Column {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = colorResource(id = R.color.green),
                    trackColor = Color(0xFFE0E0E0)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "$pointsToNext poin lagi ke Level ${data.level + 1}",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(20.dp))

            // Stats grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Post",
                    value = data.postCount.toString(),
                    icon = "📝"
                )
                StatItem(
                    label = "Reply",
                    value = data.replyCount.toString(),
                    icon = "💬"
                )
                StatItem(
                    label = "Helpful",
                    value = data.helpfulAnswerCount.toString(),
                    icon = "⭐"
                )
            }

            Spacer(Modifier.height(20.dp))

            // Badges Section
            val earnedBadges = viewModel.getEarnedBadges()
            if (earnedBadges.isNotEmpty()) {
                Text(
                    "Badge yang Diraih",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Spacer(Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(earnedBadges) { badge ->
                        BadgeItem(badge)
                    }
                }
            } else {
                // Show locked badges preview
                Text(
                    "Badge Tersedia",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Spacer(Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(BadgeDefinitions.ALL_BADGES.take(3)) { badge ->
                        LockedBadgeItem(badge)
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, icon: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.greenlight)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.width(100.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Text(icon, fontSize = 24.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.green)
            )
            Text(
                label,
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun BadgeItem(badge: Badge) {
    Surface(
        color = Color(0xFFFFF3E0),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp,
        modifier = Modifier.width(120.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(badge.icon, fontSize = 40.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                badge.name,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF424242)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                badge.description,
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                color = Color.Gray,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
fun LockedBadgeItem(badge: Badge) {
    Surface(
        color = Color(0xFFEEEEEE),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.width(120.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🔒", fontSize = 40.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                badge.name,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Spacer(Modifier.height(4.dp))
            Text(
                badge.description,
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                color = Color.LightGray,
                lineHeight = 12.sp
            )
        }
    }
}
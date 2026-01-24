package com.example.banksampah

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.banksampah.component.UserProfileImage
import com.example.banksampah.data.LeaderboardEntry
import com.example.banksampah.viewmodel.GamificationViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardApp(navController: NavHostController) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("🏆 All Time", "📅 Bulan Ini")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🏆 Leaderboard") },
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

            Column(modifier = Modifier.fillMaxSize()) {
                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = colorResource(id = R.color.green),
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        Box(
                            Modifier
                                .tabIndicatorOffset(tabPositions[selectedTab])
                                .height(4.dp)
                                .background(Color.White, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 16.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                // Content based on selected tab
                LeaderboardContent(isMonthly = selectedTab == 1)
            }
        }
    }
}


@Composable
fun LeaderboardContent(isMonthly: Boolean) {
    val viewModel: GamificationViewModel = viewModel()
    val leaderboardState by viewModel.leaderboard.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(isMonthly) {
        viewModel.loadLeaderboard(20, isMonthly)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    if (isMonthly) "📅 Top Bulan Ini" else "🌟 Top Contributors",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.green)
                )
                Text(
                    if (isMonthly) "Poin yang dikumpulkan bulan ini" else "Berdasarkan Total Poin",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (val state = leaderboardState) {
            is GamificationViewModel.LeaderboardState.Loading -> {
                LoadingLeaderboard()
            }

            is GamificationViewModel.LeaderboardState.Success -> {
                if (state.entries.isEmpty()) {
                    EmptyLeaderboard(isMonthly)
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(state.entries) { index, entry ->
                            EnhancedLeaderboardCard(
                                entry = entry,
                                rank = index + 1,
                                isCurrentUser = entry.uid == currentUserId,
                                isMonthly = isMonthly
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }

            is GamificationViewModel.LeaderboardState.Error -> {
                ErrorLeaderboard(
                    message = state.message,
                    onRetry = { viewModel.loadLeaderboard(20, isMonthly) }
                )
            }
        }

    }
}

@Composable
fun EnhancedLeaderboardCard(
    entry: LeaderboardEntry,
    rank: Int,
    isCurrentUser: Boolean,
    isMonthly: Boolean
) {
    val context = LocalContext.current

    val backgroundColor = when (rank) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> if (isCurrentUser) {
            colorResource(id = R.color.greenlight)
        } else {
            Color.White.copy(alpha = 0.95f)
        }
    }

    val borderColor = if (isCurrentUser) {
        colorResource(id = R.color.green)
    } else {
        Color.Transparent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isCurrentUser) {
                    Modifier.border(2.dp, borderColor, RoundedCornerShape(16.dp))
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (rank <= 3) 6.dp else 2.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Badge - HANYA ANGKA
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        when (rank) {
                            1 -> Brush.radialGradient(
                                colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                            )
                            2 -> Brush.radialGradient(
                                colors = listOf(Color(0xFFC0C0C0), Color(0xFF808080))
                            )
                            3 -> Brush.radialGradient(
                                colors = listOf(Color(0xFFCD7F32), Color(0xFF8B4513))
                            )
                            else -> Brush.radialGradient(
                                colors = listOf(
                                    colorResource(id = R.color.green).copy(alpha = 0.3f),
                                    colorResource(id = R.color.green).copy(alpha = 0.1f)
                                )
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                // ✅ HANYA TAMPILKAN ANGKA UNTUK SEMUA RANK
                Text(
                    text = "#$rank",
                    fontSize = if (rank <= 3) 18.sp else 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (rank) {
                        1 -> Color(0xFF8B4513) // Dark brown untuk gold
                        2 -> Color(0xFF424242) // Dark gray untuk silver
                        3 -> Color.White // White untuk bronze
                        else -> Color.Black
                    }
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Profile Photo
            UserProfileImage(
                uid = entry.uid,
                size = 56.dp,
                showAdminBadge = true
            )

            Spacer(modifier = Modifier.width(12.dp))

            // User Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = entry.displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (isCurrentUser) {
                        Text(
                            text = "YOU",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier
                                .background(
                                    colorResource(id = R.color.green),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isMonthly) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text("🚀", fontSize = 12.sp)
                            Text(
                                "Lv ${entry.level}",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (entry.badges.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text("🏅", fontSize = 12.sp)
                            Text(
                                "${entry.badges.size}",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${entry.totalPoints}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.green)
                )
                Text(
                    text = "poin",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun LoadingLeaderboard() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = colorResource(id = R.color.green),
                modifier = Modifier.size(48.dp)
            )
            Text(
                "Memuat leaderboard...",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun EmptyLeaderboard(isMonthly: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text("🏆", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Belum Ada Data",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                if (isMonthly)
                    "Belum ada aktivitas bulan ini. Ayo mulai berkontribusi!"
                else
                    "Leaderboard akan muncul saat ada pengguna yang aktif",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun ErrorLeaderboard(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Red.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                message,
                fontSize = 14.sp,
                color = Color.Red,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.green)
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Coba Lagi")
            }
        }
    }
}
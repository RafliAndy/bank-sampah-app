package com.example.banksampah

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.banksampah.component.Berita
import com.example.banksampah.component.BottomBar
import com.example.banksampah.component.LoginMenu
import com.example.banksampah.component.MainTopBar
import com.example.banksampah.viewmodel.AuthViewModel
import com.example.banksampah.repository.NewsRepository
import com.example.banksampah.ui.theme.BankSampahTheme
import com.example.banksampah.viewmodel.NewsViewModel
// ===== TAMBAHKAN IMPORT INI =====
import com.example.banksampah.component.MainEdukasiSection
import com.example.banksampah.data.Badge
import com.example.banksampah.viewmodel.GamificationViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            BankSampahTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun BankSampahApp(navController: NavHostController, authViewModel: AuthViewModel) {
    val authState = authViewModel.authState.observeAsState()

    Scaffold(
        bottomBar = {
            BottomBar(navController = navController, authViewModel = authViewModel)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Background image
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "background",
                modifier = Modifier.fillMaxSize(),
            )

            // Konten utama
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MainTopBar(navController)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 12.dp)
                ) {
                    if (authState.value is AuthViewModel.AuthState.LoggedOut) {
                        LoginMenu(navController)
                    }

                    NewsSection()
                    MainEdukasiSection(navController = navController)
                }
            }
        }
    }
}

@Composable
fun NewsSection() {
    val newsViewModel: NewsViewModel = viewModel()
    val newsState by newsViewModel.newsState.collectAsState()

    LaunchedEffect(Unit) {
        newsViewModel.loadNews()
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Newspaper,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = colorResource(id = R.color.green)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Berita Terkini Terkait Sampah di Indonesia",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (val state = newsState) {
            is NewsViewModel.NewsState.Loading -> {
                LoadingNewsCard()
            }
            is NewsViewModel.NewsState.Success -> {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.news) { item ->
                        Berita(news = item) // Gunakan komponen yang diperbaiki
                    }
                }
            }
            is NewsViewModel.NewsState.Error -> {
                ErrorNewsCard(
                    message = state.message,
                    errorType = state.errorType,
                    onRetry = { newsViewModel.loadNews() }
                )
            }
        }
    }
}

@Composable
fun LoadingNewsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.greenlight)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = colorResource(id = R.color.green),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Memuat berita terbaru...",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun ErrorNewsCard(
    message: String,
    errorType: NewsRepository.ErrorType,
    onRetry: () -> Unit
) {
    val icon: ImageVector = when (errorType) {
        NewsRepository.ErrorType.NO_INTERNET -> Icons.Default.WifiOff
        NewsRepository.ErrorType.TIMEOUT -> Icons.Default.WifiOff
        else -> Icons.Default.Warning
    }

    val iconColor = when (errorType) {
        NewsRepository.ErrorType.NO_INTERNET -> Color(0xFFFF9800)
        NewsRepository.ErrorType.TIMEOUT -> Color(0xFFFF9800)
        NewsRepository.ErrorType.SERVER_ERROR -> Color(0xFFF44336)
        else -> Color(0xFF9E9E9E)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3E0)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Error",
                modifier = Modifier.size(56.dp),
                tint = iconColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when (errorType) {
                    NewsRepository.ErrorType.NO_INTERNET -> "Tidak Ada Koneksi"
                    NewsRepository.ErrorType.TIMEOUT -> "Koneksi Timeout"
                    NewsRepository.ErrorType.SERVER_ERROR -> "Server Bermasalah"
                    NewsRepository.ErrorType.PARSING_ERROR -> "Format Berubah"
                    NewsRepository.ErrorType.UNKNOWN -> "Terjadi Kesalahan"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF424242)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                fontSize = 14.sp,
                color = Color(0xFF757575),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.green)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retry",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Coba Lagi",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (errorType == NewsRepository.ErrorType.NO_INTERNET) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "💡 Pastikan WiFi atau data seluler aktif",
                    fontSize = 12.sp,
                    color = Color(0xFF9E9E9E),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun UserGamificationStats(uid: String) {
    val viewModel: GamificationViewModel = viewModel()
    val gamificationState by viewModel.userGamification.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadUserGamification()
    }

    when (val state = gamificationState) {
        is GamificationViewModel.GamificationState.Success -> {
            val data = state.data

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Level & Points
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Level ${data.level}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorResource(id = R.color.green)
                            )
                            Text("${data.totalPoints} poin",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }

                        // Streak badge
                        if (data.currentStreak > 0) {
                            Surface(
                                color = Color(0xFFFF9800),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("🔥", fontSize = 16.sp)
                                    Text("${data.currentStreak} hari",
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Progress bar ke level berikutnya
                    val progress = viewModel.getLevelProgress()
                    val pointsToNext = viewModel.getPointsToNextLevel()

                    Column {
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = colorResource(id = R.color.green)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "$pointsToNext poin lagi ke Level ${data.level + 1}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Stats grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem("Post", data.postCount.toString())
                        StatItem("Reply", data.replyCount.toString())
                        StatItem("Helpful", data.helpfulAnswerCount.toString())
                    }

                    Spacer(Modifier.height(16.dp))

                    // Badges
                    if (data.badges.isNotEmpty()) {
                        Text("Badge", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(viewModel.getEarnedBadges()) { badge ->
                                BadgeItem(badge)
                            }
                        }
                    }
                }
            }
        }
        is GamificationViewModel.GamificationState.Loading -> {
            // Loading indicator
        }
        is GamificationViewModel.GamificationState.Error -> {
            // Error message
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun BadgeItem(badge: Badge) {
    Surface(
        color = Color(0xFFFFF3E0),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(badge.icon, fontSize = 32.sp)
            Text(badge.name, fontSize = 10.sp, textAlign = TextAlign.Center)
        }
    }
}
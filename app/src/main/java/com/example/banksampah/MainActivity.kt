package com.example.banksampah

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.banksampah.component.Berita
import com.example.banksampah.component.BottomBar
import com.example.banksampah.component.LoginMenu
import com.example.banksampah.component.MainTopBar
import com.example.banksampah.model.AuthViewModel
import com.example.banksampah.repository.NewsRepository
import com.example.banksampah.ui.theme.BankSampahTheme
import com.example.banksampah.viewmodel.NewsViewModel
// ===== TAMBAHKAN IMPORT INI =====
import com.example.banksampah.component.MainEdukasiSection

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
        Text(
            text = "Berita Terkini Terkait Sampah di Indonesia!",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        when (val state = newsState) {
            is NewsViewModel.NewsState.Loading -> {
                LoadingNewsCard()
            }
            is NewsViewModel.NewsState.Success -> {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.news) { item ->
                        Berita(news = item)
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
package com.example.banksampah

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import com.example.banksampah.component.Berita
import com.example.banksampah.component.BottomBar
import com.example.banksampah.component.Edukasi
import com.example.banksampah.component.LoginMenu
import com.example.banksampah.component.MainTopBar
import com.example.banksampah.model.AuthViewModel
import com.example.banksampah.data.dummyListTittle
import com.example.banksampah.data.dummyNewsItems
import com.example.banksampah.ui.theme.BankSampahTheme


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

    Scaffold (bottomBar = { BottomBar(navController = navController, authViewModel = authViewModel)}) { paddingValues ->
        Column (
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(paddingValues)
        ) {
            MainTopBar(navController)
            Column (modifier = Modifier
                .verticalScroll(rememberScrollState())
                .weight(1f)
                .padding(bottom = 12.dp))   {


                if (authState.value is AuthViewModel.AuthState.LoggedOut){
                    LoginMenu(navController)
                }

                NewsSection()
                MainEdukasi()
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun BankSampahAppPreview() {
    BankSampahTheme {
        AppNavigation()
    }
}


@Composable
fun MainEdukasi() {
    Column (modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
        Text("Ketahuilah!!!",fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Column (verticalArrangement = Arrangement.spacedBy(10.dp)) {
            dummyListTittle.forEach {
                Edukasi(listTittle = it)
            }
        }
    }
}

@Composable
fun NewsSection() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Berita Terkini Terkait Sampah di Indonesia!",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(dummyNewsItems) { item ->
                Berita(newsItems = item)
            }
        }

    }
}

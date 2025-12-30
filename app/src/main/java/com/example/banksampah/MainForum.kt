package com.example.banksampah

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.banksampah.component.ForumList
import com.example.banksampah.component.MainTopBar
import com.example.banksampah.model.AuthViewModel

@Composable
fun MainForumApp(navController: NavHostController, authViewModel: AuthViewModel) {

    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthViewModel.AuthState.LoggedOut ->
                navController.navigate(Routes.MAIN_LOGIN)
            else -> Unit
        }
    }

    Scaffold(bottomBar = { BottomTextButton(navController) }) { paddingValues ->
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
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                MainTopBar(navController)
                Column (modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .weight(1f)
                    .padding(bottom = 12.dp))   {
                    HorizontalDivider(modifier = Modifier.fillMaxWidth(),
                        thickness = 1.dp,
                        color = Color.Black)

                    Back(navController)

                    Box(modifier = Modifier.weight(1f)) {
                        ForumList(navController)
                    }
                }
            }
        }
    }
}


@Composable
fun Back(navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.green))
            .padding(10.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .wrapContentWidth()
                .padding(top = 5.dp, bottom = 5.dp)
                .clickable {
                    navController.navigate(Routes.HOME)
                }

            ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier.size(30.dp),
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(4.dp)) // Spasi lebih baik
            Text(
                text = "Forum",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun BottomTextButton(navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            , // Padding eksternal
        contentAlignment = Alignment.Center // Pusatkan Column
    ) {
        Column(
            modifier = Modifier
                .width(250.dp)
                .background(
                    color = colorResource(id = R.color.green),
                    shape = RoundedCornerShape(100) // Bentuk rounded
                )
                .padding(16.dp), // Padding internal
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Tambah Pertanyaan",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp)) // Jarak antara teks dan tombol

            Button(
                onClick = {
                    navController.navigate(Routes.QUESTION)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.greenlight)
                ),
                shape = RoundedCornerShape(50) // Tombol bulat
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah",
                    modifier = Modifier.size(30.dp),
                    tint = Color.Black
                )
            }
        }
    }
}


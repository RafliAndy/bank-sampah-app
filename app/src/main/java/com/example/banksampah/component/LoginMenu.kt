package com.example.banksampah.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.banksampah.R
import com.example.banksampah.Routes
import com.example.banksampah.ui.theme.BankSampahTheme

@Composable
fun LoginMenu(navController: NavHostController) {
    Column(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Ayo Gabung Jadi Anggota Bank Sampah",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        Text(
            "Lindungi lingkungan dan raih berbagai manfaat dengan bergabung sebagai anggota bank sampah",
            fontSize = 15.sp,
            maxLines = 2,
            textAlign = TextAlign.Center
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(30.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Button Daftar
            Button(
                onClick = {
                    navController.navigate(Routes.MAIN_REGISTER)
                },
                modifier = Modifier
                    .width(150.dp)
                    .height(50.dp),
                contentPadding = PaddingValues(
                    start = 39.dp,
                    end = 39.dp
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.green),
                    contentColor = Color.White
                ),
                border = BorderStroke(2.dp, Color.Black)
            ) {
                Text("Daftar")
            }

            // Button Login
            Button(
                onClick = {
                    navController.navigate(Routes.MAIN_LOGIN)
                },
                modifier = Modifier
                    .width(150.dp)
                    .height(50.dp),
                contentPadding = PaddingValues(
                    start = 39.dp,
                    end = 39.dp
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = colorResource(id = R.color.green)
                ),
                border = BorderStroke(2.dp, Color.Black)
            ) {
                Text("Login")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginMenuPreview() {
    BankSampahTheme {
        LoginMenu(navController = rememberNavController())
    }
}
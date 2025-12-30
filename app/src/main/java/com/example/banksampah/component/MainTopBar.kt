package com.example.banksampah.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.banksampah.R
import com.example.banksampah.Routes
import com.example.banksampah.ui.theme.BankSampahTheme

@Composable
fun MainTopBar(navController: NavHostController) {
    Row (modifier = Modifier
        .fillMaxWidth()
        .background(colorResource(id = R.color.green))
        .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){
        Image(
            painter = painterResource(id = R.drawable.header),
            contentDescription = "Header",

            )
        Button(
            onClick = {
                navController.navigate(Routes.MAIN_FORUM)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.green)
            )
        ) {
            Box{
            Icon(
                painter = painterResource(id = R.drawable.ic_massage),
                contentDescription = "chat",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = "Forum",
                fontSize = 12.sp,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 30.dp)
            )
        } }
    }
}

@Preview(showBackground = true)
@Composable
fun MainTopBarPreview() {
    BankSampahTheme {
        MainTopBar(navController = rememberNavController())
    }
}
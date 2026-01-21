package com.example.banksampah.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.banksampah.R
import com.example.banksampah.Routes
import com.example.banksampah.viewmodel.NotificationViewModel

@Composable
fun MainTopBar(navController: NavHostController) {
    val notificationViewModel: NotificationViewModel = viewModel()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.green))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.header),
            contentDescription = "Header",
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Notification Button with Badge
            Box {
                IconButton(
                    onClick = { navController.navigate(Routes.NOTIFICATIONS) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifikasi",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Unread badge
                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .offset(x = 24.dp, y = 8.dp)
                            .size(18.dp)
                            .background(Color.Red, androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Forum Button (existing)
            Button(
                onClick = { navController.navigate(Routes.MAIN_FORUM) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.green)
                )
            ) {
                Box {
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
                }
            }
        }
    }
}
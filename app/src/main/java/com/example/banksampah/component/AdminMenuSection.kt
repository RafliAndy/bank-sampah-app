package com.example.banksampah.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.banksampah.R
import com.example.banksampah.Routes
import com.example.banksampah.data.UserRole

@Composable
fun AdminMenuSection(navController: NavHostController, userRole: UserRole) {
    if (userRole != UserRole.ADMIN && userRole != UserRole.KADER) return

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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = colorResource(id = R.color.green),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = if (userRole == UserRole.ADMIN) "Menu Admin" else "Menu Kader",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.green)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Menu items untuk Kader dan Admin
            if (userRole == UserRole.KADER || userRole == UserRole.ADMIN) {
                AdminMenuItem(
                    icon = Icons.Default.PhotoAlbum,
                    title = "Kelola Album",
                    description = "Kelola album kegiatan",
                    onClick = { navController.navigate(Routes.ADMIN_ALBUMS) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                AdminMenuItem(
                    icon = Icons.Default.Book,
                    title = "Kelola Edukasi",
                    description = "Kelola artikel edukasi",
                    onClick = { navController.navigate(Routes.ADMIN_EDUKASI) }
                )
            }

            // Menu khusus Admin
            if (userRole == UserRole.ADMIN) {
                Spacer(modifier = Modifier.height(12.dp))

                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color.LightGray
                )

                Text(
                    text = "Khusus Admin",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                AdminMenuItem(
                    icon = Icons.Default.People,
                    title = "Kelola User",
                    description = "Kelola semua user aplikasi",
                    onClick = { navController.navigate(Routes.ADMIN_USER_MANAGEMENT) },
                    iconColor = Color(0xFFF44336)
                )
            }
        }
    }
}

@Composable
fun AdminMenuItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    iconColor: Color = colorResource(id = R.color.green)
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.greenlight)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )

                Column {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = description,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}
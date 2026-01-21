package com.example.banksampah.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.banksampah.R
import com.example.banksampah.Routes
import com.example.banksampah.data.BottomBarItem
import com.example.banksampah.viewmodel.AuthViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person

@Composable
fun BottomBar(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel
) {
    val bottomNavigation = listOf(
        BottomBarItem(
            title = R.string.home,
            icon = Icons.Default.Home,
            route = Routes.HOME
        ),
        BottomBarItem(
            title = R.string.profile,
            icon = Icons.Default.Person,
            route = Routes.PROFILE
        )
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Floating Bottom Bar Container - LEBIH BESAR
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp), // Increased padding
        contentAlignment = Alignment.Center
    ) {
        // Background dengan shadow - LEBIH TINGGI
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(85.dp), // Increased from 70dp to 85dp
            shape = RoundedCornerShape(42.dp), // Increased corner radius
            color = Color.White,
            tonalElevation = 12.dp, // Increased elevation
            shadowElevation = 16.dp // Increased shadow
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp), // Increased horizontal padding
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                bottomNavigation.forEach { item ->
                    ModernBottomBarItem(
                        item = item,
                        isSelected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ModernBottomBarItem(
    item: BottomBarItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Animasi warna
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected)
            colorResource(id = R.color.green)
        else
            Color.Transparent,
        animationSpec = tween(durationMillis = 300),
        label = "background_color"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected)
            Color.White
        else
            Color.Gray,
        animationSpec = tween(durationMillis = 300),
        label = "content_color"
    )

    // Animasi scale
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f, // Increased scale
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(25.dp)) // Increased corner radius
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 28.dp, vertical = 16.dp), // Increased padding
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(28.dp) // Increased from 24dp to 28dp
            )

            // Tampilkan teks hanya jika selected
            if (isSelected) {
                Spacer(modifier = Modifier.width(10.dp)) // Increased spacing
                Text(
                    text = androidx.compose.ui.res.stringResource(item.title),
                    color = contentColor,
                    fontSize = 16.sp, // Increased from 14sp to 16sp
                    fontWeight = FontWeight.Bold // Changed to Bold for more emphasis
                )
            }
        }
    }
}
package com.example.banksampah.data

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomBarItem(
    @StringRes val title: Int,
    val icon: ImageVector,
    val route: String
)
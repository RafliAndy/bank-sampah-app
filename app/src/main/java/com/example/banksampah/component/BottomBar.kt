package com.example.banksampah.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.banksampah.R
import com.example.banksampah.Routes
import com.example.banksampah.model.AuthViewModel
import com.example.banksampah.data.BottomBarItem


@Composable
fun BottomBar(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel
)  {

    val bottomNavigation = listOf(
        BottomBarItem(
            title = R.string.home,
            icon = Icons.Default.Home,
            route = Routes.HOME),
        BottomBarItem(
            title = R.string.profile,
            icon = Icons.Default.Person,
            route = Routes.PROFILE)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        modifier = modifier,
        containerColor = Color.Black,
    ) {
        bottomNavigation.forEach { item ->
            NavigationBarItem(
                modifier = Modifier.weight(1f).padding(start = 25.dp, end = 25.dp),
                icon = {
                    // Custom layout untuk icon dan label
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(top = 12.dp, bottom = 12.dp)
                            .fillMaxWidth()
                        ,

                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = stringResource(item.title),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = stringResource(item.title),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                },
                label = { Box {} },
                selected = currentRoute == item.route,
                onClick = {
                    // Navigasi ke route yang sesuai
                    navController.navigate(item.route) {
                        // Konfigurasi navigasi
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colorResource(R.color.white),
                    selectedTextColor = colorResource(R.color.white),
                    indicatorColor = colorResource(R.color.darkgreen),
                    unselectedIconColor = colorResource(R.color.white),
                    unselectedTextColor = colorResource(R.color.white)

                )
                )
        }
    }
}


package com.example.banksampah

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.banksampah.component.MainQuestion
import com.example.banksampah.model.AuthViewModel

object Routes {
    const val HOME = "home"
    const val MAIN_LOGIN = "login"
    const val MAIN_REGISTER = "register"
    const val PROFILE = "profile"
    const val MAIN_FORUM = "forum"
    const val QUESTION = "question"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            BankSampahApp(navController, authViewModel)
        }
        composable(Routes.MAIN_LOGIN) {
            MainLoginApp(navController, authViewModel)
        }
        composable(Routes.MAIN_REGISTER) {
            MainRegisterApp(navController, authViewModel)
        }
        composable(Routes.MAIN_FORUM) {
            MainForumApp(navController, authViewModel)
        }
        composable(Routes.QUESTION) {
            MainQuestion(navController)
        }
        composable(Routes.PROFILE) {
            MainProfileApp(navController, authViewModel)
        }
    }
}

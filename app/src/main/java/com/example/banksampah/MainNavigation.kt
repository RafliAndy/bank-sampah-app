package com.example.banksampah

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.banksampah.component.MainQuestion
import com.example.banksampah.model.AuthViewModel

object Routes {
    // Main Routes
    const val HOME = "home"
    const val MAIN_LOGIN = "login"
    const val MAIN_REGISTER = "register"
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"

    // Forum Routes
    const val MAIN_FORUM = "forum"
    const val QUESTION = "question"
    const val FORUM_DETAIL = "forum_detail/{postId}"

    // Edukasi Routes
    const val EDUKASI_DETAIL = "edukasi_detail/{edukasiId}"
    const val TENTANG_BANK_SAMPAH = "tentang_bank_sampah"
    const val ADMIN_EDUKASI = "admin_edukasi"
    const val ADMIN_GALLERY = "admin_gallery"

    // Helper functions
    fun forumDetail(postId: String) = "forum_detail/$postId"
    fun edukasiDetail(edukasiId: String) = "edukasi_detail/$edukasiId"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        // ============ MAIN ROUTES ============
        composable(Routes.HOME) {
            BankSampahApp(navController, authViewModel)
        }

        composable(Routes.MAIN_LOGIN) {
            MainLoginApp(navController, authViewModel)
        }

        composable(Routes.MAIN_REGISTER) {
            MainRegisterApp(navController, authViewModel)
        }

        composable(Routes.PROFILE) {
            MainProfileApp(navController, authViewModel)
        }

        composable(Routes.EDIT_PROFILE) {
            EditProfileScreen(navController)
        }

        // ============ FORUM ROUTES ============
        composable(Routes.MAIN_FORUM) {
            MainForumApp(navController, authViewModel)
        }

        composable(Routes.QUESTION) {
            MainQuestion(navController)
        }

        composable(
            route = Routes.FORUM_DETAIL,
            arguments = listOf(
                navArgument("postId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            ForumDetail(
                navController = navController,
                authViewModel = authViewModel,
                postId = postId
            )
        }

        // ============ EDUKASI ROUTES ============

        // Edukasi Detail (User View)
        composable(
            route = Routes.EDUKASI_DETAIL,
            arguments = listOf(
                navArgument("edukasiId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val edukasiId = backStackEntry.arguments?.getString("edukasiId") ?: ""
            EdukasiDetailScreen(
                navController = navController,
                edukasiId = edukasiId
            )
        }

        // Tentang Bank Sampah - Gallery View
        composable(Routes.TENTANG_BANK_SAMPAH) {
            TentangBankSampahScreen(navController = navController)
        }

        // Admin Edukasi Management (Admin Only)
        composable(Routes.ADMIN_EDUKASI) {
            AdminEdukasiScreen(navController = navController)
        }

        // Admin Gallery Management (Admin Only)
        composable(Routes.ADMIN_GALLERY) {
            AdminGalleryScreen(navController = navController)
        }
    }
}
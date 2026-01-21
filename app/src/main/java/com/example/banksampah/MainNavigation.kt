package com.example.banksampah

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.banksampah.component.MainQuestion
import com.example.banksampah.viewmodel.AuthViewModel

object Routes {
    // Main Routes
    const val HOME = "home"
    const val MAIN_LOGIN = "login"
    const val MAIN_REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"
    const val NOTIFICATIONS = "notifications"


    // Forum Routes
    const val MAIN_FORUM = "forum"
    const val QUESTION = "question"
    const val FORUM_DETAIL = "forum_detail/{postId}"

    // Edukasi Routes
    const val EDUKASI_DETAIL = "edukasi_detail/{edukasiId}"
    const val ADMIN_EDUKASI = "admin_edukasi"

    // Album/Gallery Routes (Tentang Bank Sampah)
    const val TENTANG_BANK_SAMPAH = "tentang_bank_sampah"
    const val ALBUM_DETAIL = "album_detail/{albumId}"
    const val ADMIN_ALBUMS = "admin_albums"
    const val ADMIN_ALBUM_PHOTOS = "admin_album_photos/{albumId}"

    // Helper functions
    fun forumDetail(postId: String) = "forum_detail/$postId"
    fun edukasiDetail(edukasiId: String) = "edukasi_detail/$edukasiId"
    fun albumDetail(albumId: String) = "album_detail/$albumId"
    fun adminAlbumPhotos(albumId: String) = "admin_album_photos/$albumId"
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

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordApp(navController, authViewModel)
        }

        composable(Routes.PROFILE) {
            MainProfileApp(navController, authViewModel)
        }

        composable(Routes.EDIT_PROFILE) {
            EditProfileScreen(navController)
        }
        composable(Routes.NOTIFICATIONS) {
            NotificationScreen(navController = navController)
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

        // Admin Edukasi Management (Admin Only)
        composable(Routes.ADMIN_EDUKASI) {
            AdminEdukasiScreen(navController = navController)
        }

        // ============ ALBUM/GALLERY ROUTES (TENTANG BANK SAMPAH) ============

        // List Album - User View
        composable(Routes.TENTANG_BANK_SAMPAH) {
            TentangBankSampahScreen(navController = navController)
        }

        // Album Detail - User View
        composable(
            route = Routes.ALBUM_DETAIL,
            arguments = listOf(
                navArgument("albumId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId") ?: ""
            AlbumDetailScreen(
                navController = navController,
                albumId = albumId
            )
        }

        // Admin: Manage Albums
        composable(Routes.ADMIN_ALBUMS) {
            AdminAlbumsScreen(navController = navController)
        }

        // Admin: Manage Photos in Album
        composable(
            route = Routes.ADMIN_ALBUM_PHOTOS,
            arguments = listOf(
                navArgument("albumId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId") ?: ""
            AdminAlbumPhotosScreen(
                navController = navController,
                albumId = albumId
            )
        }
    }
}
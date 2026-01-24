package com.example.banksampah.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.banksampah.data.User
import com.example.banksampah.data.UserRole
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * Data class untuk menyimpan informasi profil user
 */
data class UserProfileData(
    val profilePhotoUrl: String = "",
    val isAdmin: Boolean = false,
    val displayName: String = ""
)

/**
 * Composable untuk menampilkan foto profil user dengan badge admin (jika admin)
 * @param uid User ID dari Firebase
 * @param size Ukuran foto profil (default 40.dp)
 * @param showAdminBadge Tampilkan badge admin atau tidak (default true)
 * @param modifier Modifier tambahan
 */
@Composable
fun UserProfileImage(
    uid: String,
    size: Dp = 40.dp,
    showAdminBadge: Boolean = true,
    modifier: Modifier = Modifier
) {
    var userData by remember(uid) { mutableStateOf<UserProfileData?>(null) }
    val context = LocalContext.current

    // Fetch user data dari Firebase
    LaunchedEffect(uid) {
        if (uid.isNotEmpty()) {
            val userRef = FirebaseDatabase.getInstance().getReference("users/$uid")
            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val photoUrl = snapshot.child("profilePhotoUrl").getValue(String::class.java) ?: ""
                    val isAdmin = snapshot.child("isAdmin").getValue(Boolean::class.java) ?: false
                    userData = UserProfileData(photoUrl, isAdmin)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error - set default values
                    userData = UserProfileData()
                }
            })
        }
    }

    Box(
        modifier = modifier.size(size)
    ) {
        // Profile Image Container
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(Color.LightGray)
                .then(
                    // Tambahkan border biru untuk admin
                    if (userData?.isAdmin == true && showAdminBadge) {
                        Modifier.border(2.dp, Color(0xFF2196F3), CircleShape)
                    } else {
                        Modifier
                    }
                )
        ) {
            if (userData?.profilePhotoUrl?.isNotEmpty() == true) {
                // Tampilkan foto dari Cloudinary/URL
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(userData?.profilePhotoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(size / 3),
                                strokeWidth = 2.dp,
                                color = Color.Gray
                            )
                        }
                    },
                    error = {
                        // Fallback ke icon default jika gagal load
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Default Profile",
                            modifier = Modifier
                                .size(size * 0.6f)
                                .align(Alignment.Center),
                            tint = Color.Black
                        )
                    }
                )
            } else {
                // Icon default jika belum ada foto
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Default Profile",
                    modifier = Modifier
                        .size(size * 0.6f)
                        .align(Alignment.Center),
                    tint = Color.Black
                )
            }
        }

        // Admin Badge (verified icon di pojok kanan bawah)
        if (userData?.isAdmin == true && showAdminBadge) {
            Box(
                modifier = Modifier
                    .size(size * 0.35f)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(Color(0xFF2196F3))
                    .padding(2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Admin Badge",
                    tint = Color.White,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * Composable untuk menampilkan nama user dengan badge admin (jika admin)
 * @param uid User ID dari Firebase
 * @param authorName Nama author (fallback jika data belum load)
 * @param fontSize Ukuran font (default 14.sp)
 * @param fontWeight Font weight (default Bold)
 */
@Composable
fun UserNameWithBadge(
    uid: String,
    authorName: String,
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Bold
) {
    var isAdmin by remember(uid) { mutableStateOf(false) }
    var userRole by remember(uid) { mutableStateOf(UserRole.USER) }

    // Check user role
    LaunchedEffect(uid) {
        if (uid.isNotEmpty()) {
            val userRef = FirebaseDatabase.getInstance().getReference("users/$uid")
            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    userRole = user?.getRoleType() ?: UserRole.USER
                }

                override fun onCancelled(error: DatabaseError) {
                    userRole = UserRole.USER
                }
            })
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = authorName.ifBlank { "Anonymous" },
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = when(userRole) {
                UserRole.ADMIN -> Color(0xFFF44336)
                UserRole.KADER -> Color(0xFFFF9800)
                UserRole.USER -> Color.Black
            }
        )

        // Badge untuk Admin dan Kader
        when(userRole) {
            UserRole.ADMIN -> {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Admin",
                    tint = Color(0xFFF44336),
                    modifier = Modifier.size(fontSize.value.dp)
                )
            }
            UserRole.KADER -> {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Kader",
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(fontSize.value.dp)
                )
            }
            else -> Unit
        }
    }
}
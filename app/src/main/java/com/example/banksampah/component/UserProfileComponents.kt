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

data class UserProfileData(
    val profilePhotoUrl: String = "",
    val role: UserRole = UserRole.USER,
    val displayName: String = ""
)

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
                    val roleString = snapshot.child("role").getValue(String::class.java) ?: "USER"
                    val role = try {
                        UserRole.valueOf(roleString)
                    } catch (e: Exception) {
                        // Backward compatibility: cek isAdmin
                        val isAdmin = snapshot.child("isAdmin").getValue(Boolean::class.java) ?: false
                        if (isAdmin) UserRole.ADMIN else UserRole.USER
                    }
                    userData = UserProfileData(photoUrl, role)
                }

                override fun onCancelled(error: DatabaseError) {
                    userData = UserProfileData()
                }
            })
        }
    }

    Box(modifier = modifier.size(size)) {
        // Profile Image Container
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(Color.LightGray)
                .then(
                    // Border sesuai role
                    when (userData?.role) {
                        UserRole.ADMIN -> Modifier.border(2.dp, Color(0xFFF44336), CircleShape)
                        UserRole.KADER -> Modifier.border(2.dp, Color(0xFFFF9800), CircleShape)
                        else -> Modifier
                    }
                )
        ) {
            if (userData?.profilePhotoUrl?.isNotEmpty() == true) {
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

        // Badge di pojok kanan bawah (ADMIN atau KADER)
        if (showAdminBadge && (userData?.role == UserRole.ADMIN || userData?.role == UserRole.KADER)) {
            Box(
                modifier = Modifier
                    .size(size * 0.35f)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(
                        when (userData?.role) {
                            UserRole.ADMIN -> Color(0xFFF44336)
                            UserRole.KADER -> Color(0xFFFF9800)
                            else -> Color.Gray
                        }
                    )
                    .padding(2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = when (userData?.role) {
                        UserRole.ADMIN -> "Admin Badge"
                        UserRole.KADER -> "Kader Badge"
                        else -> "Badge"
                    },
                    tint = Color.White,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun UserNameWithBadge(
    uid: String,
    authorName: String,
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Bold
) {
    var userRole by remember(uid) { mutableStateOf(UserRole.USER) }

    // Check user role
    LaunchedEffect(uid) {
        if (uid.isNotEmpty()) {
            val userRef = FirebaseDatabase.getInstance().getReference("users/$uid")
            userRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val roleString = snapshot.child("role").getValue(String::class.java) ?: "USER"
                    userRole = try {
                        UserRole.valueOf(roleString)
                    } catch (e: Exception) {
                        // Backward compatibility
                        val isAdmin = snapshot.child("isAdmin").getValue(Boolean::class.java) ?: false
                        if (isAdmin) UserRole.ADMIN else UserRole.USER
                    }
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
        // ✅ FIX: Pastikan warna SELALU di-set, tidak bergantung kondisi
        Text(
            text = authorName.ifBlank { "Anonymous" },
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = when(userRole) {
                UserRole.ADMIN -> Color(0xFFF44336) // Merah untuk Admin
                UserRole.KADER -> Color(0xFFFF9800) // Orange untuk Kader
                UserRole.USER -> Color.Black // Hitam untuk User biasa
            }
        )

        // Badge icon untuk Admin dan Kader
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
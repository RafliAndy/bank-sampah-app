package com.example.banksampah.component

//Question.kt
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.banksampah.R
import com.example.banksampah.Routes
import com.example.banksampah.data.ForumPost
import com.example.banksampah.viewmodel.uploadToCloudinary
import com.example.banksampah.ui.theme.BankSampahTheme
import com.example.banksampah.viewmodel.GamificationViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

@Composable
fun MainQuestion(navController: NavHostController){
    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ){
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "background",
                modifier = Modifier.fillMaxSize()
            )
            Column {
                Question(navController)
            }
        }

    }

}

    @Composable
    fun Question(navController: NavHostController,
                 gamificationViewModel: GamificationViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    ) {
        var title by remember { mutableStateOf("") }
        var bodytext by remember { mutableStateOf("") }
        var imageUri by remember { mutableStateOf<Uri?>(null) }
        var uploading by remember { mutableStateOf(false) }

        val context = LocalContext.current

        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) imageUri = uri
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorResource(id = R.color.green))
                .padding(start = 10.dp, end = 10.dp, bottom = 5.dp, top = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clickable {
                        navController.navigate(Routes.MAIN_FORUM)
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    modifier = Modifier
                        .size(30.dp),
                    tint = Color.Black,

                    )
            }


            // Send button
            Button(
                onClick = {
                    // Validasi
                    if (title.isBlank() || bodytext.isBlank()) {
                        Toast.makeText(context, "Judul dan pertanyaan harus diisi", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val user = FirebaseAuth.getInstance().currentUser

                    if (user == null) {
                        Toast.makeText(context, "User belum login", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    uploading = true

                    val uid = user.uid

                    val usersRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
                    usersRef.child("fullName").get().addOnSuccessListener {snapshot ->
                        val authorName = snapshot.getValue(String::class.java) ?: "Anonymous"
                        if (imageUri != null) {
                            uploadToCloudinary(
                                uri = imageUri!!,
                                onSuccess = { downloadUrl ->
                                    // Setelah upload sukses, simpan data post
                                    usersRef.child("fullName").get().addOnSuccessListener { snapshot ->
                                        val authorName = snapshot.getValue(String::class.java) ?: "Anonymous"
                                        createPost(
                                            title = title,
                                            body = bodytext,
                                            imageUrl = downloadUrl,
                                            uid = uid,
                                            authorName = authorName,
                                            onComplete = {
                                                uploading = false
                                                Toast.makeText(context, "Post terkirim", Toast.LENGTH_SHORT).show()
                                                navController.navigate(Routes.MAIN_FORUM)
                                            },
                                            onError = { err ->
                                                uploading = false
                                                Toast.makeText(context, "Gagal kirim: $err", Toast.LENGTH_SHORT).show()
                                            },
                                            gamificationViewModel = gamificationViewModel

                                        )
                                    }.addOnFailureListener { e ->
                                        uploading = false
                                        Toast.makeText(context, "Gagal ambil nama user: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onError = { errorMessage ->
                                    uploading = false
                                    Toast.makeText(context, "Upload gambar gagal: $errorMessage", Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            // tanpa gambar
                            createPost(
                                title = title,
                                body = bodytext,
                                imageUrl = null,
                                uid = uid,
                                authorName = authorName,
                                onComplete = {
                                    uploading = false
                                    Toast.makeText(context, "Post terkirim", Toast.LENGTH_SHORT).show()
                                    navController.navigate(Routes.MAIN_FORUM)
                                },
                                onError = { err ->
                                    uploading = false
                                    Toast.makeText(context, "Gagal kirim: $err", Toast.LENGTH_SHORT).show()
                                },
                                gamificationViewModel = gamificationViewModel
                            )
                        }
                        }.addOnFailureListener { e ->
                        uploading = false
                        Toast.makeText(context, "Gagal ambil nama user: ${e.message}", Toast.LENGTH_SHORT).show()
                    }

                },
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(2.dp, Color.Black),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF87C37D))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "send",
                    tint = Color.Black,)
                Text(text = if (uploading) "Mengirim..." else "Kirim", fontSize = 16.sp, modifier = Modifier.padding(start = 5.dp))
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    BasicTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = Color(0xFFF0F0F0),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                        singleLine = false,
                        decorationBox = { innerTextField ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Create,
                                    contentDescription = "Create",
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))

                                Box(modifier = Modifier.weight(1f)) {
                                    if (title.isEmpty()) {
                                        Text(
                                            "Judul...",
                                            color = Color.Black,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        }
                    )
                }
            }
                HorizontalDivider(modifier = Modifier.fillMaxWidth(),
                    thickness = 2.dp,
                    color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Input field with user icon
                    BasicTextField(
                        value = bodytext,
                        onValueChange = { bodytext = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                            .heightIn(min = 150.dp),
                        decorationBox = { innerTextField ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = Color(0xFFF0F0F0),
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Create,
                                    contentDescription = "User",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            if (bodytext.isEmpty()) {
                                                Text(
                                                    "Ketik pertanyaan Anda...",
                                                    color = Color.Gray,
                                                    fontSize = 14.sp
                                                )
                                            }
                                            innerTextField()
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(25.dp)
                                                .clickable { /* Aksi ketika diklik */ }
                                                .background(Color.Transparent)
                                        ) {
                                        }
                                    }
                                }

                            }
                        },
                    )
                }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { launcher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black)
                ) {
                    Text("Pilih Gambar")

                }
                Spacer(modifier = Modifier.width(8.dp))
                imageUri?.let { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = "Preview gambar",
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
            }
        }

    private fun createPost(
        title: String,
        body: String,
        imageUrl: String?,
        uid: String,
        authorName: String,
        onComplete: () -> Unit,
        onError: (String) -> Unit,
        gamificationViewModel: GamificationViewModel
    ) {
        val postsRef = FirebaseDatabase.getInstance().getReference("posts")
        // key push otomatis
        val key = postsRef.push().key ?: UUID.randomUUID().toString()
        val post = ForumPost(
            id = key,
            title = title,
            body = body,
            uid = uid,
            imageUrl = imageUrl,
            authorName = authorName,
            timestamp = System.currentTimeMillis()
        )

        postsRef.child(key).setValue(post)
            .addOnSuccessListener {
                // ===== AUTO AWARD POINTS =====
                gamificationViewModel.awardPointsForNewPost()

                onComplete()
            }
            .addOnFailureListener { e -> onError(e.message ?: "Unknown error") }

    }


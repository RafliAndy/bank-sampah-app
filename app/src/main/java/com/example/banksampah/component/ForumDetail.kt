package com.example.banksampah

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.banksampah.component.CloudinaryImage
import com.example.banksampah.component.MainTopBar
import com.example.banksampah.component.formatTimeAgo
import com.example.banksampah.data.ForumPost
import com.example.banksampah.data.ForumReply
import com.example.banksampah.model.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Composable
fun ForumDetail(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    postId: String
) {
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    var post by remember { mutableStateOf<ForumPost?>(null) }
    var replies by remember { mutableStateOf<List<ForumReply>>(emptyList()) }
    var replyText by remember { mutableStateOf("") }
    var replyingTo by remember { mutableStateOf<Pair<String?, String?>>(null to null) } // (replyId, authorName)

    val currentUser = FirebaseAuth.getInstance().currentUser
    var currentUserName by remember { mutableStateOf("") }

    // Dapatkan nama user dari Firebase
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { uid ->
            val userRef = FirebaseDatabase.getInstance().getReference("users/$uid/fullName")
            userRef.get().addOnSuccessListener { snapshot ->
                currentUserName = snapshot.getValue(String::class.java)
                    ?: currentUser.displayName
                            ?: "Anonymous"
            }.addOnFailureListener {
                currentUserName = currentUser.displayName ?: "Anonymous"
            }
        }
    }

    // Load post data
    LaunchedEffect(postId) {
        val postRef = FirebaseDatabase.getInstance().getReference("posts/$postId")
        postRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                post = snapshot.getValue(ForumPost::class.java)?.apply {
                    if (id.isEmpty()) id = snapshot.key ?: postId
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Load replies - PERBAIKAN: Load semua replies tanpa filter
    LaunchedEffect(postId) {
        val repliesRef = FirebaseDatabase.getInstance().getReference("replies")

        repliesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val replyList = mutableListOf<ForumReply>()
                for (replySnapshot in snapshot.children) {
                    val reply = replySnapshot.getValue(ForumReply::class.java)
                    reply?.let {
                        if (it.id.isEmpty()) {
                            it.id = replySnapshot.key ?: ""
                        }
                        // Filter hanya reply untuk post ini
                        if (it.postId == postId) {
                            replyList.add(it)
                        }
                    }
                }
                // Urutkan berdasarkan timestamp
                replies = replyList.sortedBy { it.timestamp }

                // Debug log
                println("DEBUG - Total replies loaded: ${replyList.size}")
                replyList.forEach { r ->
                    println("DEBUG - Reply: id=${r.id}, postId=${r.postId}, parentId=${r.parentReplyId}, level=${r.level}")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                println("DEBUG - Error loading replies: ${error.message}")
            }
        })
    }

    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthViewModel.AuthState.LoggedOut ->
                navController.navigate(Routes.MAIN_LOGIN)
            else -> Unit
        }
    }

    Scaffold(
        bottomBar = {
            ReplyInputBar(
                replyText = replyText,
                onReplyTextChange = { replyText = it },
                replyingTo = replyingTo,
                onCancelReply = { replyingTo = null to null },
                onSendReply = {
                    if (replyText.isNotBlank() && currentUser != null) {
                        val repliesRef = FirebaseDatabase.getInstance().getReference("replies")
                        val newReplyRef = repliesRef.push()

                        val newReply = ForumReply(
                            id = newReplyRef.key ?: "",
                            postId = postId,
                            parentReplyId = replyingTo.first,
                            body = replyText,
                            uid = currentUser.uid,
                            authorName = currentUserName, // Gunakan nama yang sudah diload
                            timestamp = System.currentTimeMillis(),
                            level = if (replyingTo.first == null) 0 else 1
                        )

                        println("DEBUG - Sending reply: $newReply")

                        newReplyRef.setValue(newReply).addOnSuccessListener {
                            println("DEBUG - Reply sent successfully")
                        }.addOnFailureListener { e ->
                            println("DEBUG - Failed to send reply: ${e.message}")
                        }

                        replyText = ""
                        replyingTo = null to null
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "background",
                modifier = Modifier.fillMaxSize()
            )

            Column(modifier = Modifier.fillMaxSize()) {
                MainTopBar(navController)

                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = Color.Black
                )

                TopBar(navController)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    post?.let { PostDetail(it) }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "${replies.size} Balasan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    RepliesList(
                        replies = replies,
                        onReplyClick = { replyId, authorName ->
                            replyingTo = replyId to authorName
                        }
                    )

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun TopBar(navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.green))
            .padding(10.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .wrapContentWidth()
                .padding(vertical = 5.dp)
                .clickable {
                    navController.popBackStack()
                }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier.size(30.dp),
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Detail Forum",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PostDetail(post: ForumPost) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(id = R.color.greenlight)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Person Icon",
                        modifier = Modifier
                            .size(28.dp)
                            .align(Alignment.Center),
                        tint = Color.Black
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = post.authorName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatTimeAgo(post.timestamp),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = post.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = post.body,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            post.imageUrl?.let { imageUrl ->
                Spacer(modifier = Modifier.height(16.dp))
                CloudinaryImage(
                    imageUrl = imageUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                )
            }
        }
    }
}

@Composable
fun RepliesList(
    replies: List<ForumReply>,
    onReplyClick: (String, String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        RenderReplies(
            replies = replies,
            parentId = null,
            onReplyClick = onReplyClick,
            level = 0
        )
    }
}


@Composable
fun ReplyItem(
    reply: ForumReply,
    onReplyClick: (String, String) -> Unit,
    level: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min) // ⭐ KUNCI UTAMA
    ) {

        // 🔗 GARIS PENGHUBUNG (VERTICAL THREAD LINE)
        if (level > 0) {
            Box(
                modifier = Modifier
                    .width(16.dp * level)
                    .fillMaxHeight(),
                contentAlignment = Alignment.TopEnd
            ) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(Color.LightGray)
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (level == 0)
                    colorResource(id = R.color.greenlight)
                else
                    Color(0xFFE8F5E9)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.align(Alignment.Center),
                            tint = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = reply.authorName.ifBlank { "Anonymous" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = formatTimeAgo(reply.timestamp),
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = reply.body,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                TextButton(
                    onClick = { onReplyClick(reply.id, reply.authorName) },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        "Balas",
                        fontSize = 12.sp,
                        color = colorResource(id = R.color.green)
                    )
                }
            }
        }
    }
}


@Composable
fun ReplyInputBar(
    replyText: String,
    onReplyTextChange: (String) -> Unit,
    replyingTo: Pair<String?, String?>,
    onCancelReply: () -> Unit,
    onSendReply: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        if (replyingTo.first != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(id = R.color.greenlight))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Membalas ${replyingTo.second}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                TextButton(
                    onClick = onCancelReply,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Batal",
                        fontSize = 12.sp,
                        color = Color.Red
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = replyText,
                onValueChange = onReplyTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Tulis balasan...") },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorResource(id = R.color.green),
                    unfocusedBorderColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onSendReply,
                enabled = replyText.isNotBlank(),
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (replyText.isNotBlank())
                            colorResource(id = R.color.green)
                        else
                            Color.Gray,
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun RenderReplies(
    replies: List<ForumReply>,
    parentId: String?,
    onReplyClick: (String, String) -> Unit,
    level: Int
) {
    val children = replies.filter { it.parentReplyId == parentId }

    children.forEach { reply ->
        ReplyItem(
            reply = reply,
            onReplyClick = onReplyClick,
            level = level
        )

        // 🔁 RECURSIVE CALL
        RenderReplies(
            replies = replies,
            parentId = reply.id,
            onReplyClick = onReplyClick,
            level = level + 1
        )
    }
}

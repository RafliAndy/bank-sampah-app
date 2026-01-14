package com.example.banksampah

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import com.example.banksampah.component.UserProfileImage
import com.example.banksampah.component.UserNameWithBadge
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.banksampah.component.CloudinaryImage
import com.example.banksampah.component.MainTopBar
import com.example.banksampah.component.formatTimeAgo
import com.example.banksampah.data.ForumPost
import com.example.banksampah.data.ForumReply
import com.example.banksampah.model.AuthViewModel
import com.example.banksampah.viewmodel.ForumViewModel
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
    var replyingTo by remember { mutableStateOf<Pair<String?, String?>>(null to null) }
    var isAdmin by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val currentUser = FirebaseAuth.getInstance().currentUser
    var currentUserName by remember { mutableStateOf("") }

    // ViewModel untuk handle delete
    val forumViewModel: ForumViewModel = viewModel()
    val deleteState by forumViewModel.deleteState.collectAsState()

    // Check if user is admin
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { uid ->
            val adminRef = FirebaseDatabase.getInstance().getReference("users/$uid/isAdmin")
            adminRef.get().addOnSuccessListener { snapshot ->
                isAdmin = snapshot.getValue(Boolean::class.java) ?: false
            }
        }
    }

    // Dapatkan nama user dari Firebase
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { uid ->
            val userRef = FirebaseDatabase.getInstance().getReference("users/$uid/displayName")
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

    // Load replies
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
                        if (it.postId == postId) {
                            replyList.add(it)
                        }
                    }
                }
                replies = replyList.sortedBy { it.timestamp }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Handle delete result
    LaunchedEffect(deleteState) {
        when (val state = deleteState) {
            is ForumViewModel.DeleteState.Success -> {
                Toast.makeText(
                    context,
                    "Postingan berhasil dihapus",
                    Toast.LENGTH_SHORT
                ).show()
                forumViewModel.resetDeleteState()
                navController.popBackStack()
            }
            is ForumViewModel.DeleteState.Error -> {
                Toast.makeText(
                    context,
                    state.message,
                    Toast.LENGTH_SHORT
                ).show()
                forumViewModel.resetDeleteState()
            }
            else -> Unit
        }
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
                            authorName = currentUserName,
                            timestamp = System.currentTimeMillis(),
                            level = if (replyingTo.first == null) 0 else 1
                        )

                        newReplyRef.setValue(newReply)
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

                TopBar(
                    navController = navController,
                    post = post,
                    currentUser = currentUser,
                    isAdmin = isAdmin,
                    onDeleteClick = { showDeleteDialog = true }
                )

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

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Postingan") },
            text = {
                Text("Apakah Anda yakin ingin menghapus postingan ini? Semua balasan juga akan dihapus.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        forumViewModel.deletePost(postId)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Red
                    )
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun TopBar(
    navController: NavHostController,
    post: ForumPost?,
    currentUser: com.google.firebase.auth.FirebaseUser?,
    isAdmin: Boolean,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.green))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button and title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .clickable {
                    navController.popBackStack()
                }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier.size(26.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Detail Forum",
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Delete Button dengan Elevation
        if (post != null && (post.uid == currentUser?.uid || isAdmin)) {
            Card(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(
                        onClick = onDeleteClick
                    ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 3.dp,
                    pressedElevation = 1.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Red.copy(alpha = 0.9f)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Post",
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Text(
                        text = "Hapus",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
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
                // Gunakan komponen foto profil baru dengan badge admin
                UserProfileImage(
                    uid = post.uid,
                    size = 45.dp,
                    showAdminBadge = true
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    // Gunakan komponen nama dengan badge admin
                    UserNameWithBadge(
                        uid = post.uid,
                        authorName = post.authorName,
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
            .height(IntrinsicSize.Min)
    ) {
        // Garis penghubung (vertical thread line)
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
                    // Gunakan komponen foto profil baru dengan badge admin
                    UserProfileImage(
                        uid = reply.uid,
                        size = 32.dp,
                        showAdminBadge = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        // Gunakan komponen nama dengan badge admin
                        UserNameWithBadge(
                            uid = reply.uid,
                            authorName = reply.authorName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
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
                    imageVector = Icons.AutoMirrored.Filled.Send,
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

        // Recursive call untuk nested replies
        RenderReplies(
            replies = replies,
            parentId = reply.id,
            onReplyClick = onReplyClick,
            level = level + 1
        )
    }
}
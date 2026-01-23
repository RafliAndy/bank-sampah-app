package com.example.banksampah

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
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
import com.example.banksampah.data.VoteType
import com.example.banksampah.viewmodel.AuthViewModel
import com.example.banksampah.viewmodel.ForumViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.banksampah.repository.NotificationRepository
import com.example.banksampah.viewmodel.GamificationViewModel

@Composable
fun ForumDetail(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    postId: String
) {
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current
    val gamificationViewModel: GamificationViewModel = viewModel()

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

                        newReplyRef.setValue(newReply).addOnSuccessListener {
                            gamificationViewModel.awardPointsForNewReply()
                            // ===== BUAT NOTIFIKASI =====
                            CoroutineScope(Dispatchers.IO).launch {
                                val notificationRepo = NotificationRepository()

                                if (replyingTo.first == null) {
                                    // Reply langsung ke post - notifikasi ke pemilik post
                                    post?.let { p ->
                                        notificationRepo.createPostReplyNotification(
                                            postId = postId,
                                            postOwnerId = p.uid,
                                            replyId = newReply.id
                                        )
                                    }
                                } else {
                                    // Reply ke reply - notifikasi ke pemilik reply yang dibalas
                                    repliesRef.child(replyingTo.first!!).get().addOnSuccessListener { snapshot ->
                                        val parentReplyOwnerId = snapshot.child("uid").getValue(String::class.java)

                                        if (parentReplyOwnerId != null) {
                                            CoroutineScope(Dispatchers.IO).launch {
                                                notificationRepo.createNestedReplyNotification(
                                                    postId = postId,
                                                    parentReplyId = replyingTo.first!!,
                                                    parentReplyOwnerId = parentReplyOwnerId,
                                                    newReplyId = newReply.id
                                                )
                                            }
                                        }
                                    }
                                }
                            }
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
                    post?.let {
                        PostDetail(
                            post = it,
                            viewModel = gamificationViewModel
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "${replies.size} Balasan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    post?.let { p ->
                        RepliesList(
                            replies = replies,
                            postOwnerId = p.uid,
                            gamificationViewModel = gamificationViewModel,
                            onReplyClick = { replyId, authorName ->
                                replyingTo = replyId to authorName
                            }
                        )
                    }


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
fun PostDetail(post: ForumPost, viewModel: GamificationViewModel) {
    val currentUser = FirebaseAuth.getInstance().currentUser

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
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Upvote/Downvote buttons
                VotingButtons(
                    targetId = post.id,
                    targetType = VoteType.POST,
                    upvotes = post.upvotes,
                    downvotes = post.downvotes,
                    onUpvote = {
                        viewModel.upvotePost(post.id)
                    },
                    onDownvote = {
                        viewModel.downvotePost(post.id)
                    },
                    enabled = currentUser?.uid != post.uid
                )

                // Net score
                Text(
                    text = "${post.upvotes - post.downvotes} poin",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.green)
                )

            }

        }
    }
}


@Composable
fun VotingButtons(
    targetId: String,
    targetType: VoteType,
    upvotes: Int,
    downvotes: Int,
    onUpvote: () -> Unit,
    onDownvote: () -> Unit,
    enabled: Boolean = true
) {
    val viewModel: GamificationViewModel = viewModel()
    val isVoting by viewModel.isVoting.collectAsState()
    val voteStates by viewModel.voteStates.collectAsState()

    LaunchedEffect(targetId) {
        viewModel.getUserVote(targetId, targetType)
    }
    val userVote = voteStates[targetId] ?: 0

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Upvote button
        Button(
            onClick = onUpvote,
            enabled = enabled && !isVoting,
            colors = ButtonDefaults.buttonColors(
                // ✅ Filled green jika user sudah upvote
                containerColor = if (userVote == 1) Color(0xFF4CAF50) else Color.Transparent,
                contentColor = if (userVote == 1) Color.White else Color(0xFF4CAF50),
                disabledContainerColor = Color.LightGray.copy(alpha = 0.3f),
                disabledContentColor = Color.Gray
            ),
            modifier = Modifier.height(36.dp),
            border = if (userVote != 1) BorderStroke(1.dp, Color(0xFF4CAF50)) else null,
            shape = RoundedCornerShape(18.dp),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            if (isVoting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = if (userVote == 1) Color.White else Color(0xFF4CAF50)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Upvote",
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                upvotes.toString(),
                fontSize = 14.sp,
                fontWeight = if (userVote == 1) FontWeight.Bold else FontWeight.Normal
            )
        }

        // Downvote button
        Button(
            onClick = onDownvote,
            enabled = enabled && !isVoting,
            colors = ButtonDefaults.buttonColors(
                // ✅ Filled red jika user sudah downvote
                containerColor = if (userVote == -1) Color(0xFFF44336) else Color.Transparent,
                contentColor = if (userVote == -1) Color.White else Color(0xFFF44336),
                disabledContainerColor = Color.LightGray.copy(alpha = 0.3f),
                disabledContentColor = Color.Gray
            ),
            modifier = Modifier.height(36.dp),
            border = if (userVote != -1) BorderStroke(1.dp, Color(0xFFF44336)) else null,
            shape = RoundedCornerShape(18.dp),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            if (isVoting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = if (userVote == -1) Color.White else Color(0xFFF44336)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "Downvote",
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                downvotes.toString(),
                fontSize = 14.sp,
                fontWeight = if (userVote == -1) FontWeight.Bold else FontWeight.Normal
            )
        }

    }
}
@Composable
fun RepliesList(
    replies: List<ForumReply>,
    postOwnerId: String,
    onReplyClick: (String, String) -> Unit,
    gamificationViewModel: GamificationViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        RenderReplies(
            replies = replies,
            parentId = null,
            onReplyClick = onReplyClick,
            level = 0,
            postOwnerId = postOwnerId,
            gamificationViewModel = gamificationViewModel
        )
    }
}


@Composable
fun ReplyItem(
    reply: ForumReply,
    postOwnerId: String,
    onReplyClick: (String, String) -> Unit,
    level: Int,
    gamificationViewModel: GamificationViewModel
) {

    val currentUser = FirebaseAuth.getInstance().currentUser
    val isPostOwner = currentUser?.uid == postOwnerId

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
                containerColor = if (reply.isMarkedHelpful)
                    Color(0xFFE8F5E9) // Hijau muda untuk helpful answer
                else if (level == 0)
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Voting buttons
                    VotingButtons(
                        targetId = reply.id,
                        targetType = VoteType.REPLY,
                        upvotes = reply.upvotes,
                        downvotes = reply.downvotes,
                        onUpvote = { gamificationViewModel.upvoteReply(reply.id) },
                        onDownvote = { gamificationViewModel.downvoteReply(reply.id) },
                        enabled = currentUser?.uid != reply.uid
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // "Balas" button
                        TextButton(
                            onClick = { onReplyClick(reply.id, reply.authorName) },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("Balas", fontSize = 12.sp)
                        }

                        // "Tandai Helpful" button (only for post owner, only on level 0)
                        if (isPostOwner && level == 0 && !reply.isMarkedHelpful) {
                            TextButton(
                                onClick = {
                                    gamificationViewModel.markReplyAsHelpful(reply.postId, reply.id)
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Helpful",
                                    modifier = Modifier.size(14.dp),
                                    tint = colorResource(id = R.color.green)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Helpful", fontSize = 12.sp)
                            }
                        }

                        // Badge jika sudah marked as helpful
                        if (reply.isMarkedHelpful) {
                            Surface(
                                color = Color(0xFF4CAF50),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = Color.White
                                    )
                                    Text(
                                        "Helpful Answer",
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
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
    level: Int,
    postOwnerId: String,
    gamificationViewModel: GamificationViewModel
) {
    val children = replies.filter { it.parentReplyId == parentId }

    children.forEach { reply ->
        ReplyItem(
            reply = reply,
            postOwnerId = postOwnerId,
            onReplyClick = onReplyClick,
            level = level,
            gamificationViewModel = gamificationViewModel
        )

        RenderReplies(
            replies = replies,
            parentId = reply.id,
            onReplyClick = onReplyClick,
            level = level + 1,
            postOwnerId = postOwnerId,
            gamificationViewModel = gamificationViewModel
        )
    }
}

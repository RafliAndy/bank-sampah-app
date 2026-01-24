package com.example.banksampah

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.banksampah.component.UserProfileImage
import com.example.banksampah.data.User
import com.example.banksampah.data.UserRole
import com.example.banksampah.viewmodel.UserManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserManagementScreen(navController: NavHostController) {
    val viewModel: UserManagementViewModel = viewModel()
    val usersState by viewModel.usersState.collectAsState()
    val statsState by viewModel.statsState.collectAsState()
    val operationState by viewModel.operationState.collectAsState()
    val context = LocalContext.current

    var selectedUser by remember { mutableStateOf<User?>(null) }
    var showRoleDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Handle operation result
    LaunchedEffect(operationState) {
        when (val state = operationState) {
            is UserManagementViewModel.OperationState.Success -> {
                Toast.makeText(context, "Operasi berhasil!", Toast.LENGTH_SHORT).show()
                viewModel.resetOperationState()
                selectedUser = null
                showRoleDialog = false
                showDeleteDialog = false
            }
            is UserManagementViewModel.OperationState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                viewModel.resetOperationState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelola User") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.green),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Statistics Cards
                when (val state = statsState) {
                    is UserManagementViewModel.StatsState.Success -> {
                        StatsSection(stats = state.stats)
                    }
                    else -> Unit
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Users List
                when (val state = usersState) {
                    is UserManagementViewModel.UsersState.Loading -> {
                        LoadingSection()
                    }

                    is UserManagementViewModel.UsersState.Success -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(600.dp),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.users) { user ->
                                UserManagementCard(
                                    user = user,
                                    onChangeRole = {
                                        selectedUser = user
                                        showRoleDialog = true
                                    },
                                    onDelete = {
                                        selectedUser = user
                                        showDeleteDialog = true
                                    },
                                    isProcessing = operationState is UserManagementViewModel.OperationState.Loading
                                )
                            }
                        }
                    }

                    is UserManagementViewModel.UsersState.Error -> {
                        ErrorSection(
                            message = state.message,
                            onRetry = { viewModel.loadUsers() }
                        )
                    }
                }
            }
        }
    }

    // Role Change Dialog
    if (showRoleDialog && selectedUser != null) {
        RoleChangeDialog(
            user = selectedUser!!,
            onDismiss = {
                showRoleDialog = false
                selectedUser = null
            },
            onConfirm = { newRole ->
                viewModel.updateUserRole(selectedUser!!.uid, newRole)
            },
            isLoading = operationState is UserManagementViewModel.OperationState.Loading
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && selectedUser != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                selectedUser = null
            },
            title = { Text("Hapus User") },
            text = {
                Text("Apakah Anda yakin ingin menghapus user \"${selectedUser!!.fullName}\"? Tindakan ini tidak dapat dibatalkan.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteUser(selectedUser!!.uid)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red),
                    enabled = operationState !is UserManagementViewModel.OperationState.Loading
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        selectedUser = null
                    }
                ) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun StatsSection(stats: com.example.banksampah.repository.UserStats) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Total User",
            value = stats.total.toString(),
            icon = Icons.Default.People,
            color = colorResource(id = R.color.green)
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Admin",
            value = stats.adminCount.toString(),
            icon = Icons.Default.AdminPanelSettings,
            color = Color(0xFFF44336)
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            title = "Kader",
            value = stats.kaderCount.toString(),
            icon = Icons.Default.SupervisorAccount,
            color = Color(0xFFFF9800)
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "User Biasa",
            value = stats.userCount.toString(),
            icon = Icons.Default.Person,
            color = Color(0xFF2196F3)
        )
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun UserManagementCard(
    user: User,
    onChangeRole: () -> Unit,
    onDelete: () -> Unit,
    isProcessing: Boolean
) {
    val role = user.getRoleType()
    val roleColor = when (role) {
        UserRole.ADMIN -> Color(0xFFF44336)
        UserRole.KADER -> Color(0xFFFF9800)
        UserRole.USER -> Color(0xFF2196F3)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Photo
            UserProfileImage(
                uid = user.uid,
                size = 56.dp,
                showAdminBadge = false
            )

            Spacer(modifier = Modifier.width(12.dp))

            // User Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.fullName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = user.email,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Role Badge
                Surface(
                    color = roleColor,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = role.displayName,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Actions
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // Change Role Button
                IconButton(
                    onClick = onChangeRole,
                    enabled = !isProcessing
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Change Role",
                        tint = colorResource(id = R.color.green)
                    )
                }

                // Delete Button (only for non-admin)
                if (role != UserRole.ADMIN) {
                    IconButton(
                        onClick = onDelete,
                        enabled = !isProcessing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RoleChangeDialog(
    user: User,
    onDismiss: () -> Unit,
    onConfirm: (UserRole) -> Unit,
    isLoading: Boolean
) {
    var selectedRole by remember { mutableStateOf(user.getRoleType()) }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Ubah Role User") },
        text = {
            Column {
                Text("Pilih role baru untuk ${user.fullName}:")

                Spacer(modifier = Modifier.height(16.dp))

                UserRole.values().forEach { role ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isLoading) { selectedRole = role }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedRole == role,
                            onClick = { selectedRole = role },
                            enabled = !isLoading
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(role.displayName)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedRole) },
                enabled = !isLoading && selectedRole != user.getRoleType()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Ubah")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun LoadingSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = colorResource(id = R.color.green))
    }
}

@Composable
fun ErrorSection(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFEBEE)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.Red
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                color = Color.Red,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.green)
                )
            ) {
                Text("Coba Lagi")
            }
        }
    }
}
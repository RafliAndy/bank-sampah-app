package com.example.banksampah

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.banksampah.component.BottomBar
import com.example.banksampah.component.MainTopBar
import com.example.banksampah.model.AuthViewModel
import com.example.banksampah.ui.theme.BankSampahTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


@Composable
fun MainRegisterApp(navController: NavHostController, authViewModel: AuthViewModel) {
    Scaffold(bottomBar = { BottomBar(navController = navController, authViewModel = authViewModel) }) { paddingValues ->
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
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                MainTopBar(navController)
                MainRegister(navController, authViewModel)
            }
       }
    }
}



@Composable
fun MainRegister(navController: NavHostController, authViewModel: AuthViewModel) {
    var fullName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var nik by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }


    var errorMessage by remember { mutableStateOf("") }

    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    val database = FirebaseDatabase.getInstance().reference.child("users")
    val auth = FirebaseAuth.getInstance()


    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthViewModel.AuthState.LoggedIn ->{
                val uid = auth.currentUser?.uid ?: ""

                val userData = mapOf(
                    "fullName" to fullName,
                    "address" to address,
                    "phoneNumber" to phoneNumber,
                    "nik" to nik,
                    "email" to email,
                    "password" to password,
                    "roles" to "user"
                )

                database.child(uid).setValue(userData).addOnSuccessListener {
                    navController.navigate(Routes.HOME){
                        popUpTo(0)
                    }
                }
            }
            is AuthViewModel.AuthState.Error -> Toast.makeText(context,
                (authState.value as AuthViewModel.AuthState.Error).message, Toast.LENGTH_SHORT).show()
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Judul Form
        Text(
            text = "Formulir Pendaftaran Nasabah",
            style = MaterialTheme.typography.headlineSmall,
        )

        // Field Nama Lengkap
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Masukan Nama Lengkap", color = colorResource(id = R.color.green)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person, // Atau ganti dengan ikon yang lebih cocok
                    contentDescription = "Nama",
                    tint = colorResource(id = R.color.green)
                )
            },
            shape = RoundedCornerShape(50),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(id = R.color.green),
                unfocusedBorderColor = colorResource(id = R.color.green),
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)

        )

        // Field Alamat
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Masukan Alamat", color = colorResource(id = R.color.green)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Home, // Atau ganti dengan ikon yang lebih cocok
                    contentDescription = "Alamat",
                    tint = colorResource(id = R.color.green)
                )
            },
            shape = RoundedCornerShape(50),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(id = R.color.green),
                unfocusedBorderColor = colorResource(id = R.color.green),
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)

        )

        // Field Nomor Telepon
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Masukan No.Telp", color = colorResource(id = R.color.green)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Call, // Atau ganti dengan ikon yang lebih cocok
                    contentDescription = "Telp",
                    tint = colorResource(id = R.color.green)
                )
            },
            shape = RoundedCornerShape(50),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(id = R.color.green),
                unfocusedBorderColor = colorResource(id = R.color.green),
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)

        )

        // Field NIK
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = nik,
            onValueChange = { nik = it },
            label = { Text("Masukkan NIK", color = colorResource(id = R.color.green)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.AccountBox, // Atau ganti dengan ikon yang lebih cocok
                    contentDescription = "Nik",
                    tint = colorResource(id = R.color.green)
                )
            },
            shape = RoundedCornerShape(50),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(id = R.color.green),
                unfocusedBorderColor = colorResource(id = R.color.green),
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)

        )

        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Masukkan Email", color = colorResource(id = R.color.green)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email, // Atau ganti dengan ikon yang lebih cocok
                    contentDescription = "Nik",
                    tint = colorResource(id = R.color.green)
                )
            },
            shape = RoundedCornerShape(50),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(id = R.color.green),
                unfocusedBorderColor = colorResource(id = R.color.green),
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)

        )


        // Field Password
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Masukkan Password", color = colorResource(id = R.color.green)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock, // Atau ganti dengan ikon yang lebih cocok
                    contentDescription = "Password",
                    tint = colorResource(id = R.color.green)
                )
            },
            shape = RoundedCornerShape(50),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(id = R.color.green),
                unfocusedBorderColor = colorResource(id = R.color.green),
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)

        )

        // Tombol Daftar
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                if (fullName.isNotEmpty() && address.isNotEmpty() && phoneNumber.isNotEmpty() && nik.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()){
                    authViewModel.signup(email, password)
                } else {
                    errorMessage = "Semua data harus diisi"
                }
            },
            modifier = Modifier
                .width(150.dp)
                .height(50.dp)
                ,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.green),
                contentColor = Color.White
            )
        ) {
            Text("Daftar", fontSize = 16.sp)
        }
        if (errorMessage.isNotEmpty()){
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sudah punya akun?",
                color = Color.Black,
                fontSize = 16.sp
            )
            TextButton(
                onClick = {navController.navigate(Routes.MAIN_LOGIN)}
            ) {
                Text(
                    text = "Login Serkarang",
                    color = colorResource(id = R.color.green),
                    fontSize = 18.sp
                )
            }
        }

    }
}

@Preview
@Composable
fun MainRegisterAppPreview() {
    BankSampahTheme {
        val navController = rememberNavController()
        val authViewModel: AuthViewModel = viewModel()
        MainRegisterApp(navController = navController, authViewModel = authViewModel)
    }
}
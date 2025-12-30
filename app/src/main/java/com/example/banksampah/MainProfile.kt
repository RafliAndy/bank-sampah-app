package com.example.banksampah

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.banksampah.component.BottomBar
import com.example.banksampah.model.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Composable
fun MainProfileApp(navController: NavHostController, authViewModel: AuthViewModel) {

    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthViewModel.AuthState.LoggedOut ->
                navController.navigate(Routes.MAIN_LOGIN)
            is AuthViewModel.AuthState.Error -> Toast.makeText(context,
                (authState.value as AuthViewModel.AuthState.Error).message, Toast.LENGTH_SHORT).show()
            else -> Unit
        }
    }



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
                MainProfile(navController, authViewModel)
            }
        }
    }
}

@Composable
fun MainProfile(navController: NavHostController, authViewModel: AuthViewModel){
    var fullName by remember { mutableStateOf("Loading...") }
    val context = LocalContext.current

    val uid = FirebaseAuth.getInstance().currentUser?.uid

    if (uid != null) {
        val ref = FirebaseDatabase.getInstance().reference.child("users").child(uid)
        DisposableEffect(uid) {
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    fullName = snapshot.child("fullName").getValue(String::class.java) ?: "Nama belum diatur"
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Gagal ambil data: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
            ref.addValueEventListener(listener)

            onDispose {
                ref.removeEventListener(listener)
            }
        }
    }

    Column{
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(colorResource(id = R.color.green)),
        ) {
            Column {
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, top = 20.dp, end = 20.dp)
                ) {
                    Text(
                        text = "Profile",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Medium,
                        fontSize = 30.sp ,
                        color = Color.White,
                        modifier = Modifier
                    )
                    TextButton(
                        onClick = {
                            authViewModel.signout()
                        },
                        modifier = Modifier
                            .background(Color.Red, shape = RoundedCornerShape(50))
                    )  {
                        Text(
                            text = "Sign Out",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp ,
                            color = Color.White,
                            modifier = Modifier
                        )
                    }
                }

                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp)
                ){
                    Image(
                        painter = painterResource(id = R.drawable.profilephoto),
                        contentDescription = "Profile Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding( top = 10.dp, bottom = 10.dp)
                            .size(100.dp)
                            .border(BorderStroke(width = 5.dp, color = Color.White), shape = RoundedCornerShape(100))
                    )
                    Column (
                        modifier = Modifier
                            .padding(10.dp),

                    ) {
                        Text(
                            text = "Hello,",
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2C4E30),
                            fontSize = 20.sp
                        )
                        Text(
                            text = fullName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            modifier = Modifier
                                .width(200.dp),
                            fontSize = 25.sp,

                        )
                    }
                    IconButton(
                        onClick = {
                        },
                        modifier = Modifier
                            .background(Color.Gray, shape = RoundedCornerShape(100))
                            .size(35.dp)
                    ) {
                        Icon(

                            imageVector = Icons.Default.Settings,
                            contentDescription = "Edit Profile",
                            tint = Color.White,
                            modifier = Modifier
                                .size(28.dp)
                        )
                    }
                }
            }

        }

    }
}

//@Preview
//@Composable
//private fun MainProfilePreview() {
//    BankSampahTheme {
//        MainProfileApp(navController = rememberNavController())
//    }
//}
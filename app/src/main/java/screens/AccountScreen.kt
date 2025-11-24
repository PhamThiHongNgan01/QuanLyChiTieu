package com.example.quanlychitieu.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.*
import com.example.quanlychitieu.R

private val OrangeColor = Color(0xFFFF9800)

private val CustomColorScheme = lightColorScheme(
    primary = OrangeColor,
    onPrimary = Color.White,
    secondary = OrangeColor,
    onSecondary = Color.White,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    userId: String?,
    onBack: () -> Unit,
    onEditPassword: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var passwordVisible by remember { mutableStateOf(false) }

    // Lấy dữ liệu từ Firebase
    LaunchedEffect(userId ?: "") {
        if (!userId.isNullOrEmpty()) {
            val ref = FirebaseDatabase.getInstance().getReference("users").child(userId)
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        email = snapshot.child("email").getValue(String::class.java) ?: ""
                        password = snapshot.child("password").getValue(String::class.java) ?: ""
                        Log.d("AccountScreen", "Đọc thành công -> $email, $password")
                    }
                    loading = false
                }

                override fun onCancelled(error: DatabaseError) {
                    loading = false
                    Log.e("AccountScreen", "Firebase error: ${error.message}")
                }
            })
        } else {
            loading = false
        }
    }

    MaterialTheme(colorScheme = CustomColorScheme) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Tài khoản",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black // hoặc đổi màu chữ tùy bạn
                        )

                    },
                    navigationIcon = {
                        IconButton(onClick = { onBack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Quay lại",
                                tint = Color(0xFFFF9800) //  icon màu cam
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { onEditPassword() }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Đổi mật khẩu",
                                tint = Color(0xFFFF9800) // icon màu cam
                            )
                        }
                    },
                    windowInsets = WindowInsets(0) // loại bỏ khoảng trống phía trên
                )
            }

        ) { padding ->
            if (loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_cat),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.Gray),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.height(24.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        readOnly = true
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu"
                                )
                            }
                        },
                        readOnly = true
                    )

                    Spacer(Modifier.height(24.dp))



                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { onBack() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

package com.example.quanlychitieu.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    navController: NavController,
    userId: String
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }

    var message by remember { mutableStateOf("") }
    var currentEmail by remember { mutableStateOf("Đang tải...") }

    val database = FirebaseDatabase.getInstance().getReference("users")

    //  Lấy email khi mở màn hình
    LaunchedEffect(userId) {
        database.child(userId).child("email").get()
            .addOnSuccessListener {
                val emailFromDb = it.getValue<String>() ?: "Không có email"
                currentEmail = emailFromDb
                newEmail = emailFromDb  // Gắn vào ô nhập
            }
            .addOnFailureListener {
                currentEmail = "Lỗi tải email"
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Đổi thông tin tài khoản", color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = Color(0xFFFF9800))
                    }
                },
                windowInsets = WindowInsets(0)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // 🔹 Email hiện tại
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = "Email", tint = Color(0xFFFF9800))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Email hiện tại:  $currentEmail",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.DarkGray
                        )
                    }

                    // Email mới
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = { Text("Email mới") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    // Mật khẩu mới
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Mật khẩu mới") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Mật khẩu mới") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Xác nhận mật khẩu") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Xác nhận mật khẩu") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(24.dp))

                    // 🔥 Nút cập nhật
                    Button(
                        onClick = {
                            when {
                                newEmail.isEmpty() -> message = " Email không được rỗng!"
                                newPassword != confirmPassword -> message = " Mật khẩu không khớp!"
                                newPassword.isEmpty() -> message = " Mật khẩu không được rỗng!"
                                else -> {
                                    val updates = mapOf(
                                        "email" to newEmail,
                                        "password" to newPassword
                                    )

                                    database.child(userId)
                                        .updateChildren(updates)
                                        .addOnSuccessListener {
                                            message = "Cập nhật thành công!"
                                        }
                                        .addOnFailureListener {
                                            message = " Lỗi: ${it.message}"
                                        }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF9800),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Cập nhật", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(16.dp))

                    if (message.isNotEmpty()) {
                        Text(
                            text = message,
                            color = if (message.contains("thành công")) Color(0xFF4CAF50) else Color.Red,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

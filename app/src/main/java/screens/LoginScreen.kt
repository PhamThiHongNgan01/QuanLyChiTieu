package com.example.quanlychitieu.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.FirebaseDatabase

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("QuanLyChiTieu", android.content.Context.MODE_PRIVATE)

    // 📌 Lấy dữ liệu đã lưu (nếu có)
    var email by remember { mutableStateOf(prefs.getString("email", "") ?: "") }
    var password by remember { mutableStateOf(prefs.getString("password", "") ?: "") }
    var isRememberMe by remember { mutableStateOf(prefs.getBoolean("rememberMe", false)) }
    var message by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val database = FirebaseDatabase.getInstance("https://quanlychitieu-99d33-default-rtdb.asia-southeast1.firebasedatabase.app/")
    val usersRef = database.getReference("users")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFE0B2), Color.White)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color(0xFFFF9800), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🐾", fontSize = 24.sp)
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    "Đăng nhập",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF333333)
                )
                Text("Chào mừng bạn đến với quản lý chi tiêu", fontSize = 14.sp, color = Color.Gray)

                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mật khẩu") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description = if (passwordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu"

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    }
                )

                Spacer(Modifier.height(12.dp))

                // ✅ Checkbox Ghi nhớ
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isRememberMe,
                        onCheckedChange = { isRememberMe = it }
                    )
                    Text("Ghi nhớ đăng nhập")
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            usersRef.get()
                                .addOnSuccessListener { snapshot ->
                                    var found = false
                                    for (child in snapshot.children) {
                                        val dbEmail = child.child("email").value?.toString()
                                        val dbPass = child.child("password").value?.toString()
                                        if (dbEmail == email && dbPass == password) {
                                            found = true
                                            val userId = child.key

                                            // ✅ Lưu SharedPreferences
                                            prefs.edit()
                                                .putString("userId", userId)
                                                .putBoolean("rememberMe", isRememberMe)
                                                .apply()

                                            if (isRememberMe) {
                                                prefs.edit()
                                                    .putString("email", email)
                                                    .putString("password", password)
                                                    .apply()
                                            } else {
                                                prefs.edit()
                                                    .remove("email")
                                                    .remove("password")
                                                    .putBoolean("rememberMe", false)
                                                    .apply()
                                            }

                                            message = "Đăng nhập thành công!"
                                            onLoginSuccess()
                                            break
                                        }
                                    }
                                    if (!found) {
                                        message = "Sai email hoặc mật khẩu"
                                    }
                                }
                                .addOnFailureListener {
                                    message = "Lỗi: ${it.message}"
                                }
                        } else {
                            message = "Vui lòng nhập đầy đủ thông tin"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    Text("Đăng nhập", fontSize = 16.sp, color = Color.White)
                }

                Spacer(Modifier.height(10.dp))

                TextButton(onClick = { onNavigateToRegister() }) {
                    Text("Chưa có tài khoản? Đăng ký", color = Color(0xFFFF9800))
                }

                if (message.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Text(message, color = Color(0xFFFF5722))
                }
            }
        }
    }
}

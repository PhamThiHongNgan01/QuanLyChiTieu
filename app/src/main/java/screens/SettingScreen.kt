package com.example.quanlychitieu.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.quanlychitieu.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt", fontWeight = FontWeight.Bold) },
                actions = {
                    Text(
                        text = "Trợ giúp",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clickable {
                                navController.navigate("support")
                            }
                    )
                },
                windowInsets = WindowInsets(0) // loại bỏ khoảng trống phía trên
            )
        }
    )  { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // --- Nhóm 1 ---
            item {
                SettingItem(R.drawable.ic_account, "Tài khoản", Color(0xFFFF99CC)) {
                    navController.navigate("account")
                }
                Divider()
                SettingItem(R.drawable.ic_setting, "Cài đặt cơ bản", Color(0xFF2196F3))
                Divider()

                SettingItem(R.drawable.ic_star, "Dịch vụ Premium", Color(0xFFFFC107))
                Divider()
                SettingItem(R.drawable.ic_money, "Chi phí cố định và thu nhập định kì", Color(0xFFF44336))
                Spacer(Modifier.height(16.dp))
            }

            // --- Nhóm 2 ---
            item {
                SettingItem(R.drawable.ic_palette, "Thay đổi màu chủ đề", Color(0xFF9C27B0))
                Divider()
                SettingItem(R.drawable.ic_apps, "Thay đổi biểu tượng ứng dụng", Color(0xFF009688))
                Spacer(Modifier.height(16.dp))
            }

            // --- Nhóm 3: báo cáo ---
            item {
                SettingItem(R.drawable.ic_bar_chart, "Báo cáo trong năm", Color(0xFF3F51B5))
                Divider()
                SettingItem(R.drawable.ic_pie_chart, "Báo cáo danh mục trong năm", Color(0xFF795548))
                Divider()
                SettingItem(R.drawable.ic_report, "Báo cáo toàn kì", Color(0xFF607D8B))
                Divider()
                SettingItem(R.drawable.ic_pie_chart, "Báo cáo danh mục toàn kì", Color(0xFFE91E63))
                Divider()
                SettingItem(R.drawable.ic_trending, "Báo cáo thay đổi số dư", Color(0xFF673AB7))
                Divider()
                SettingItem(R.drawable.ic_search, "Tìm kiếm giao dịch", Color(0xFF00BCD4))
                Spacer(Modifier.height(16.dp))
            }

            // --- Nhóm cuối ---
            item {
                SettingItem(
                    R.drawable.ic_leave,
                    "Đăng xuất",
                    Color(0xFF8BC34A),
                    onClick = { showDialog = true }
                )
            }
        }
    }

    // Hộp thoại xác nhận đăng xuất
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Xác nhận đăng xuất") },
            text = { Text("Bạn có chắc chắn muốn đăng xuất không?") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true } // xoá toàn bộ backstack
                    }
                }) {
                    Text("Có")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Không")
                }
            }
        )
    }
}

@Composable
fun SettingItem(iconRes: Int, title: String, tint: Color, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = title,
            tint = tint
        )
        Spacer(Modifier.width(16.dp))
        Text(title, fontSize = 16.sp)
    }
}

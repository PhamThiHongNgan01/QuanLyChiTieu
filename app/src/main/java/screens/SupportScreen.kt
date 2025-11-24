package com.example.quanlychitieu.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.database.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("QuanLyChiTieu", android.content.Context.MODE_PRIVATE)
    val userId = prefs.getString("userId", "unknown_user") ?: "unknown_user"

    val dbRef = FirebaseDatabase.getInstance().getReference("chats/$userId/messages")

    var messages by remember { mutableStateOf(listOf<Map<String, String>>()) }
    var currentMessage by remember { mutableStateOf("") }

    //  Lắng nghe dữ liệu realtime từ Firebase
    LaunchedEffect(Unit) {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val msgList = mutableListOf<Map<String, String>>()
                for (child in snapshot.children) {
                    val sender = child.child("sender").getValue(String::class.java) ?: ""
                    val text = child.child("text").getValue(String::class.java) ?: ""
                    msgList.add(mapOf("sender" to sender, "text" to text))
                }
                messages = msgList
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // 🎨 Màu chủ đạo cam (#FF9800)
    val OrangeMain = Color(0xFFFF9800)
    val OrangeLight = Color(0xFFFFB74D)
    val GrayBackground = Color(0xFFF5F5F5)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Trò chuyện với Admin",
                        color = OrangeMain,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                windowInsets = WindowInsets(0) // loại bỏ khoảng trống phía trên
            )

        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = currentMessage,
                    onValueChange = { currentMessage = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    placeholder = { Text("Nhập tin nhắn...") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = OrangeMain,
                        unfocusedIndicatorColor = OrangeLight
                    )

                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (currentMessage.isNotBlank()) {
                            val newMsg = mapOf(
                                "sender" to "user",
                                "text" to currentMessage,
                                "timestamp" to System.currentTimeMillis().toString()
                            )
                            dbRef.push().setValue(newMsg)
                            currentMessage = ""
                        }
                    },
                    modifier = Modifier.height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeMain)
                ) {
                    Text("Gửi", color = Color.White)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(GrayBackground),
            reverseLayout = false
        ) {
            items(messages) { message ->
                val isUser = message["sender"] == "user"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isUser) OrangeLight.copy(alpha = 0.25f)
                                else Color.White,
                                shape = MaterialTheme.shapes.medium
                            )
                            .padding(12.dp)
                            .widthIn(max = 280.dp)
                    ) {
                        Text(
                            text = message["text"] ?: "",
                            color = Color.Black,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

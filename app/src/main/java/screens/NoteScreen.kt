package com.example.quanlychitieu.screens

import android.app.DatePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*
import com.example.quanlychitieu.model.Note
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(navController: NavController) {
    val context = LocalContext.current
    val database = FirebaseDatabase.getInstance().getReference("notes")

    // 👉 Lấy userId từ SharedPreferences
    val prefs = context.getSharedPreferences("QuanLyChiTieu", Context.MODE_PRIVATE)
    val currentUserId = prefs.getString("userId", null)

    var noteContent by remember { mutableStateOf("") }
    val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    var selectedDate by remember { mutableStateOf(dateFormatter.format(Date())) }

    // 👉 Background + nội dung
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            // 👉 Header: nút quay lại + tiêu đề
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = Color.Black
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "Ghi chú",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.height(39.dp))

            //  Chọn ngày
            OutlinedTextField(
                value = selectedDate,
                onValueChange = {},
                label = { Text("Ngày") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = {
                        val calendar = Calendar.getInstance()
                        val datePicker = DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val chosenDate = Calendar.getInstance()
                                chosenDate.set(year, month, dayOfMonth)
                                selectedDate = dateFormatter.format(chosenDate.time)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )
                        datePicker.show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Chọn ngày",
                            tint = Color.Gray
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 👉 Nội dung ghi chú
            OutlinedTextField(
                value = noteContent,
                onValueChange = { noteContent = it },
                label = { Text("Nội dung ghi chú") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 👉 Nút Lưu
            Button(
                onClick = {
                    if (noteContent.isNotBlank()) {
                        if (!currentUserId.isNullOrEmpty()) {
                            // Lưu note theo userId + ngày
                            val newNoteRef = database.child(currentUserId).child(selectedDate).push()
                            val noteId = newNoteRef.key ?: UUID.randomUUID().toString()
                            val note = Note(
                                id = noteId,
                                content = noteContent,
                                date = selectedDate,
                                userId = currentUserId // ✅ thêm userId
                            )

                            newNoteRef.setValue(note)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "✅ Lưu ghi chú thành công", Toast.LENGTH_SHORT).show()
                                    noteContent = ""
                                    navController.popBackStack()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "❌ Lỗi: ${it.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(context, "⚠️ Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "⚠️ Nội dung trống!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
            ) {
                Text("Lưu ghi chú", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

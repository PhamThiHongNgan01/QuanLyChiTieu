package com.example.quanlychitieu.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quanlychitieu.model.Category

@Composable
fun TransactionForm(
    note: String,
    money: String,
    onNoteChange: (String) -> Unit,
    onMoneyChange: (String) -> Unit,
    categories: List<Category>, // dùng Category từ package model
    buttonText: String,
    buttonColor: Color,
    onSubmit: (Category) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            val currentDate = remember {
                val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy (EEE)", java.util.Locale("vi"))
                dateFormat.format(java.util.Date())
            }

            OutlinedTextField(
                value = currentDate,
                onValueChange = {},
                label = { Text("Ngày") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                label = { Text("Ghi chú") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = money,
                onValueChange = onMoneyChange,
                label = { Text("Số tiền") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Danh mục", fontSize = 18.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),   // dùng weight thay vì fillMaxSize
                contentPadding = PaddingValues(8.dp)
            )
            {
                items(categories) { category ->
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                            .clickable {
                                selectedCategory = category
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = category.icon),
                            contentDescription = category.name,
                            tint = if (selectedCategory == category) Color.Black else category.color,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            category.name,
                            fontSize = 12.sp,
                            fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Nút cố định dưới cùng
        Button(
            onClick = {
                Log.d("TransactionForm", " Button clicked")
                selectedCategory?.let {
                    Log.d("TransactionForm", " Selected category: ${it.name}")
                    onSubmit(it)
                } ?: Log.d("TransactionForm", " Chưa chọn danh mục")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
        ) {
            Text(buttonText, fontSize = 16.sp, color = Color.White)
        }
    }
}

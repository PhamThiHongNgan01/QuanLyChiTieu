package com.example.quanlychitieu.model

import androidx.compose.ui.graphics.Color
import com.example.quanlychitieu.R

data class Category(
    val name: String,
    val icon: Int,   // icon resource
    val color: Color    // màu hiển thị
)

// Danh sách mẫu
val categories = listOf(
    Category("Ăn uống", R.drawable.ic_food, Color.Red),
    Category("Mua sắm", R.drawable.ic_shopping, Color.Blue),
    Category("Lương", R.drawable.ic_salary, Color.Green),
    Category("Giải trí", R.drawable.ic_entertainment, Color.Magenta),
    Category("Khác", R.drawable.ic_other, Color.Gray)
)

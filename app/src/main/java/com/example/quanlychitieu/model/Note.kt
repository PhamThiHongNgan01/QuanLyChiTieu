package com.example.quanlychitieu.model


data class Note(
    val id: String = "",
    val content: String = "",
    val date: String = "",
    val userId: String = "" // ✅ thêm userId
)
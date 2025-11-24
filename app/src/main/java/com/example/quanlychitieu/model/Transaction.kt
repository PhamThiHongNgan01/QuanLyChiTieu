package com.example.quanlychitieu.model

data class Transaction(
    val id: String = "",
    val note: String = "",
    val amount: Double = 0.0,
    val date: String = "",
    val type: String = "",
    val category: String = "",
    val userId: String = ""
) {
    // Bắt buộc cho Firebase: constructor rỗng
    constructor() : this("", "", 0.0, "", "", "")
}

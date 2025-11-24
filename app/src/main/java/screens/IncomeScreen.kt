package com.example.quanlychitieu.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.quanlychitieu.R
import com.example.quanlychitieu.model.Category
import com.example.quanlychitieu.model.Transaction
import com.google.firebase.database.FirebaseDatabase

@Composable
fun IncomeScreen(
    note: String,
    money: String,
    onNoteChange: (String) -> Unit,
    onMoneyChange: (String) -> Unit
) {
    val context = LocalContext.current

    // ✅ State đảm bảo userId luôn cập nhật
    var userId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("QuanLyChiTieu", Context.MODE_PRIVATE)
        userId = prefs.getString("userId", null)
        Log.d("IncomeScreen", "userId đọc từ SharedPreferences: $userId")
    }

    val incomeCategories = listOf(
        Category("Lương", R.drawable.ic_salary, Color(0xFF4CAF50)),
        Category("Thưởng", R.drawable.ic_bonus, Color(0xFF2196F3)),
        Category("Đầu tư", R.drawable.ic_invest, Color(0xFFFF9800)),
        Category("Kinh doanh", R.drawable.ic_business, Color(0xFFE91E63)),
        Category("Khác", R.drawable.ic_more, Color(0xFF9E9E9E))
    )

    TransactionForm(
        note = note,
        money = money,
        onNoteChange = onNoteChange,
        onMoneyChange = onMoneyChange,
        categories = incomeCategories,
        buttonText = "Tạo khoản thu",
        buttonColor = Color(0xFF004400),
        onSubmit = { selectedCategory ->
            if (userId.isNullOrEmpty()) {
                Toast.makeText(context, "❌ Không tìm thấy userId, vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show()
                return@TransactionForm
            }

            Log.d("IncomeScreen", "👉 onSubmit với category: ${selectedCategory.name}, userId: $userId")
            saveTransactionToFirebase(
                context = context,
                note = note,
                money = money,
                category = selectedCategory.name,
                type = "Thu",
                userId = userId!!,
                onSuccess = {
                    Toast.makeText(context, "✅ Tạo khoản thu thành công", Toast.LENGTH_SHORT).show()
                    onNoteChange("")
                    onMoneyChange("")
                },
                onFailure = { e ->
                    Toast.makeText(context, "❌ Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    )
}

// ✅ Hàm lưu giao dịch vào Firebase
fun saveTransactionToFirebase(
    context: Context,
    note: String,
    money: String,
    category: String,
    type: String,
    userId: String,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val databaseUrl = "https://quanlychitieu-99d33-default-rtdb.asia-southeast1.firebasedatabase.app/"
    val db = FirebaseDatabase.getInstance(databaseUrl).getReference("transactions").child(userId)

    val id = db.push().key ?: return

    val moneyValue = money.toDoubleOrNull() ?: 0.0
    val currentDate = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
        .format(java.util.Date())

    val transaction = Transaction(
        id = id,
        note = note,
        amount = moneyValue,
        date = currentDate,
        type = type,
        category = category,
        userId = userId // thêm dòng này
    )


    Log.d("Firebase", "👉 Chuẩn bị lưu: $transaction")

    db.child(id).setValue(transaction)
        .addOnSuccessListener {
            Log.d("Firebase", "✅ Lưu thành công")
            onSuccess()
        }
        .addOnFailureListener { e ->
            Log.e("Firebase", "❌ Lỗi khi lưu: ${e.message}")
            onFailure(e)
        }
}

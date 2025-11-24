package com.example.quanlychitieu.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.example.quanlychitieu.R
import com.example.quanlychitieu.model.Category
import com.example.quanlychitieu.model.Transaction
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExpenseScreen(
    note: String,
    money: String,
    onNoteChange: (String) -> Unit,
    onMoneyChange: (String) -> Unit
) {
    val context = LocalContext.current

    var totalIncome by remember { mutableStateOf(0.0) }
    var totalExpense by remember { mutableStateOf(0.0) }
    var total by remember { mutableStateOf(0.0) }

    var showOverBalanceDialog by remember { mutableStateOf(false) }
    var pendingCategory by remember { mutableStateOf<Category?>(null) }
    var pendingMoney by remember { mutableStateOf("") }
    var pendingNote by remember { mutableStateOf("") }

    val prefs = context.getSharedPreferences("QuanLyChiTieu", Context.MODE_PRIVATE)
    val userId = prefs.getString("userId", null)
    val databaseUrl =
        "https://quanlychitieu-99d33-default-rtdb.asia-southeast1.firebasedatabase.app/"

    // Load Firebase và tính tổng thu chi
    LaunchedEffect(Unit) {
        if (userId != null) {
            FirebaseDatabase.getInstance(databaseUrl)
                .getReference("transactions")
                .child(userId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var income = 0.0
                        var expense = 0.0
                        for (child in snapshot.children) {
                            val t = child.getValue(Transaction::class.java)
                            if (t != null) {
                                if (t.type.equals("Thu", true) || t.type.equals("income", true)) {
                                    income += t.amount
                                } else {
                                    expense += t.amount
                                }
                            }
                        }
                        totalIncome = income
                        totalExpense = expense
                        total = totalIncome - totalExpense
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    val expenseCategories = listOf(
        Category("Ăn uống", R.drawable.ic_food, Color(0xFFFF9800)),
        Category("Chi tiêu hàng ngày", R.drawable.ic_cart, Color(0xFF4CAF50)),
        Category("Quần áo", R.drawable.ic_clothes, Color(0xFF2196F3)),
        Category("Mỹ phẩm", R.drawable.ic_cosmetics, Color(0xFFE91E63)),
        Category("Phí giao lưu", R.drawable.ic_party, Color(0xFFFFC107)),
        Category("Y tế", R.drawable.ic_health, Color(0xFF009688)),
        Category("Giáo dục", R.drawable.ic_education, Color(0xFFF44336)),
        Category("Điện nước", R.drawable.ic_electric, Color(0xFF00BCD4)),
        Category("Đi lại", R.drawable.ic_transport, Color(0xFF795548)),
        Category("Liên lạc", R.drawable.ic_phone, Color(0xFF9E9E9E)),
        Category("Tiền nhà", R.drawable.ic_home, Color(0xFFFF5722)),
        Category("Chỉnh sửa", R.drawable.ic_edit, Color(0xFF607D8B))
    )

    fun validateMoney(input: String) {
        onMoneyChange(input)
        val cleaned = input.replace(",", "").replace(".", "")
        val moneyValue = cleaned.toDoubleOrNull() ?: return

        if (moneyValue > total) {
            showOverBalanceDialog = true
        }
    }

    TransactionForm(
        note = note,
        money = money,
        onNoteChange = onNoteChange,
        onMoneyChange = { validateMoney(it) },
        categories = expenseCategories,
        buttonText = "Tạo khoản chi",
        buttonColor = Color(0xFFFF9800),
        onSubmit = { selectedCategory ->
            val moneyValue = money.replace(",", "").replace(".", "").toDoubleOrNull() ?: -1.0
            if (moneyValue <= 0) {
                Toast.makeText(context, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show()
                return@TransactionForm
            }

            if (moneyValue > total) {
                // Lưu tạm để dialog xử lý
                pendingCategory = selectedCategory
                pendingMoney = money
                pendingNote = note
                showOverBalanceDialog = true
                return@TransactionForm
            }

            saveTransactionToFirebase(
                context,
                note,
                money,
                selectedCategory.name,
                "Chi",
                {
                    Toast.makeText(context, "Tạo khoản chi thành công", Toast.LENGTH_SHORT).show()
                    onNoteChange("")
                    onMoneyChange("")
                },
                { e ->
                    Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    )

    if (showOverBalanceDialog) {
        AlertDialog(
            onDismissRequest = { showOverBalanceDialog = false },
            title = { Text("Số dư không đủ", fontWeight = FontWeight.Bold) },
            text = { Text("Số tiền bạn nhập vượt quá số dư hiện tại (${total.toInt()} VND). Bạn có muốn tiếp tục không?") },
            confirmButton = {
                TextButton(onClick = {
                    showOverBalanceDialog = false
                    val cat = pendingCategory!!
                    saveTransactionToFirebase(
                        context,
                        pendingNote,
                        pendingMoney,
                        cat.name,
                        "Chi",
                        {
                            Toast.makeText(context, "Đã lưu giao dịch (âm)", Toast.LENGTH_SHORT).show()
                            onNoteChange("")
                            onMoneyChange("")
                        },
                        { e ->
                            Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }) {
                    Text("Có", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showOverBalanceDialog = false }) {
                    Text("Không")
                }
            }
        )
    }
}

// SAVE FIREBASE
fun saveTransactionToFirebase(
    context: Context,
    note: String,
    money: String,
    category: String,
    type: String,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    val prefs = context.getSharedPreferences("QuanLyChiTieu", Context.MODE_PRIVATE)
    val userId = prefs.getString("userId", null)

    if (userId == null) {
        onFailure(Exception("Không tìm thấy userId"))
        return
    }

    val db = FirebaseDatabase.getInstance(
        "https://quanlychitieu-99d33-default-rtdb.asia-southeast1.firebasedatabase.app/"
    ).getReference("transactions").child(userId)

    val id = db.push().key ?: return

    val moneyValue = money.replace(",", "").replace(".", "").toDoubleOrNull() ?: 0.0
    val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

    val transaction = Transaction(
        id = id,
        note = note,
        amount = moneyValue,
        date = currentDate,
        type = type,
        category = category,
        userId = userId
    )

    db.child(id).setValue(transaction)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure(it) }
}

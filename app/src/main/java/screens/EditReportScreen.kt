package com.example.quanlychitieu.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.quanlychitieu.model.Transaction
import com.google.firebase.database.*
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.BorderStroke
import java.text.SimpleDateFormat
import java.util.Locale


val Orange = Color(0xFFFF9800)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReportScreen(navController: NavHostController) {
    var transactions by remember { mutableStateOf(listOf<Transaction>()) }
    var loading by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // 🔥 Lấy userId từ SharedPreferences
    val prefs = context.getSharedPreferences("QuanLyChiTieu", Context.MODE_PRIVATE)
    val userId = prefs.getString("userId", null) ?: ""

    val databaseUrl =
        "https://quanlychitieu-99d33-default-rtdb.asia-southeast1.firebasedatabase.app/"
    val dbRef = FirebaseDatabase.getInstance(databaseUrl)
        .getReference("transactions")
        .child(userId)

    // 🔥 Lấy dữ liệu từ Firebase
    LaunchedEffect(userId) {
        if (userId.isEmpty()) {
            loading = false
            return@LaunchedEffect
        }
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Transaction>()
                for (child in snapshot.children) {
                    val tran = child.getValue(Transaction::class.java)?.copy(
                        id = child.key ?: ""
                    )
                    if (tran != null) list.add(tran)
                }

                // ✔ CHỈNH SỬA TẠI ĐÂY – SORT THEO NGÀY dd-MM-yyyy
                val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

                transactions = list.sortedByDescending { tran ->
                    try {
                        sdf.parse(tran.date)?.time ?: 0L
                    } catch (e: Exception) {
                        0L
                    }
                }


                loading = false
            }

            override fun onCancelled(error: DatabaseError) {
                println("Firebase error: ${error.message}")
                loading = false
            }
        })
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Chỉnh sửa chi tiêu", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = Orange)
                    }
                },
                windowInsets = WindowInsets(0)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp)
        ) {
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Orange)
                }
            } else {
                LazyColumn {
                    items(transactions, key = { it.id }) { tran ->
                        TransactionItem(
                            tran = tran,
                            dbRef = dbRef,
                            onSaved = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Đã lưu thay đổi thành công")
                                }
                            },
                            onDeleted = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Đã xóa giao dịch thành công")
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionItem(
    tran: Transaction,
    dbRef: DatabaseReference,
    onSaved: () -> Unit,
    onDeleted: () -> Unit = {}
) {
    var note by remember { mutableStateOf(tran.note) }
    var rawAmount by remember { mutableStateOf(tran.amount.toString()) }
    var date by remember { mutableStateOf(tran.date) }
    var type by remember { mutableStateOf(tran.type) }
    var category by remember { mutableStateOf(tran.category) }

    var expandedType by remember { mutableStateOf(false) }
    var expandedCategory by remember { mutableStateOf(false) }

    // 👉 State hiển thị dialog xác nhận xóa
    var showDeleteDialog by remember { mutableStateOf(false) }

    fun formatCurrency(value: String): String {
        val number = value.toDoubleOrNull() ?: return ""
        return DecimalFormat("#,###").format(number).replace(",", ".") + "đ"
    }

    // 👉 Dialog xác nhận xóa
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa giao dịch này không?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        dbRef.child(tran.id).removeValue()
                            .addOnSuccessListener {
                                showDeleteDialog = false
                                onDeleted() // Gọi snackbar báo thành công
                            }
                    }
                ) {
                    Text("Xóa", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            Text(
                text = "Giao dịch",
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Ghi chú
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Ghi chú") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Số tiền
            OutlinedTextField(
                value = formatCurrency(rawAmount),
                onValueChange = { rawAmount = it.filter { c -> c.isDigit() } },
                label = { Text("Số tiền") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Ngày
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Ngày") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Loại
            ExposedDropdownMenuBox(
                expanded = expandedType,
                onExpandedChange = { expandedType = !expandedType }
            ) {
                OutlinedTextField(
                    value = type,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Loại") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedType) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expandedType,
                    onDismissRequest = { expandedType = false }
                ) {
                    listOf("Thu", "Chi").forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                type = option
                                expandedType = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Danh mục
            ExposedDropdownMenuBox(
                expanded = expandedCategory,
                onExpandedChange = { expandedCategory = !expandedCategory }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Danh mục") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedCategory) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expandedCategory,
                    onDismissRequest = { expandedCategory = false }
                ) {
                    listOf("Ăn uống", "Giải trí", "Mua sắm", "Lương", "Khác").forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                category = option
                                expandedCategory = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {

                // Nút XÓA -> mở dialog
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.Red)
                ) {
                    Text("Xóa", color = Color.Red)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Nút LƯU -> cập nhật Firebase
                Button(
                    onClick = {
                        val updateMap = mapOf(
                            "note" to note,
                            "amount" to rawAmount.toDoubleOrNull(),
                            "date" to date,
                            "type" to type,
                            "category" to category
                        )
                        dbRef.child(tran.id).updateChildren(updateMap)
                            .addOnSuccessListener { onSaved() }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Orange)
                ) {
                    Text("Lưu", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

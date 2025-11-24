package com.example.quanlychitieu.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quanlychitieu.R
import com.example.quanlychitieu.model.Note
import com.example.quanlychitieu.model.Transaction
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@SuppressLint("SimpleDateFormat")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    navController: NavHostController,
    notesViewModel: NotesViewModel = viewModel()
) {
    val calendar = remember { Calendar.getInstance() }
    var selectedDate by remember { mutableStateOf(calendar.time) }
    val formatter = remember { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()) }
    val daysOfWeek = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

    // ✅ State cho transactions
    var transactions by remember { mutableStateOf(listOf<Transaction>()) }

    val context = LocalContext.current
    val prefs = context.getSharedPreferences("QuanLyChiTieu", Context.MODE_PRIVATE)
    val currentUserId = prefs.getString("userId", null)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var selectedTransactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }

// State để chứa notes
    var notesByDate by remember { mutableStateOf<Map<String, List<Note>>>(emptyMap()) }
    // ✅ Lấy dữ liệu từ Firebase Realtime Database theo userId
    LaunchedEffect(currentUserId) {
        if (!currentUserId.isNullOrEmpty()) {

            // --- Lấy notes ---
            val notesRef = FirebaseDatabase.getInstance()
                .getReference("notes")
                .child(currentUserId)

            notesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val map = mutableMapOf<String, MutableList<Note>>()
                    for (dateSnapshot in snapshot.children) { // key = "27-09-2025"
                        val dateKey = dateSnapshot.key ?: continue
                        val list = mutableListOf<Note>()
                        for (noteSnap in dateSnapshot.children) {
                            val note = noteSnap.getValue(Note::class.java)
                            if (note != null) list.add(note)
                        }
                        map[dateKey] = list
                    }
                    notesByDate = map
                }

                override fun onCancelled(error: DatabaseError) {
                    println(" Firebase error (notes): ${error.message}")
                }
            })

            // --- Lấy transactions ---
            val transactionsRef = FirebaseDatabase.getInstance()
                .getReference("transactions")
                .child(currentUserId)

            transactionsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Transaction>()
                    for (tranSnap in snapshot.children) {
                        val tran = tranSnap.getValue(Transaction::class.java)
                        if (tran != null) list.add(tran)
                    }
                    transactions = list
                }

                override fun onCancelled(error: DatabaseError) {
                    println(" Firebase error (transactions): ${error.message}")
                }
            })
        }
    }





    // State cho dialog
    var selectedNotes by remember { mutableStateOf<List<Note>>(emptyList()) }
    var showNotesDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Lịch",
                        color = Color(0xFFFF9800),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    TextButton(onClick = { navController.navigate("ghiChu") }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Ghi chú",
                                color = Color(0xFFFF9800),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.ic_note),
                                contentDescription = "Ghi chú",
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                },
                windowInsets = WindowInsets(0) // loại bỏ khoảng trống phía trên
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // --- Thanh tháng ---
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { calendar.add(Calendar.MONTH, -1) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val month = calendar.get(Calendar.MONTH) + 1
                    val year = calendar.get(Calendar.YEAR)
                    val firstDay = (calendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
                    val lastDay = (calendar.clone() as Calendar).apply {
                        set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                    }

                    Text("$month/$year", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "(${formatter.format(firstDay.time)}–${formatter.format(lastDay.time)})",
                        fontSize = 12.sp
                    )
                }
                IconButton(onClick = { calendar.add(Calendar.MONTH, 1) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
                }
            }

            // --- Header ngày trong tuần ---
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                daysOfWeek.forEachIndexed { idx, day ->
                    Text(
                        text = day,
                        color = if (idx == 6) Color.Red else Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // --- Lưới lịch ---
            val firstDayOfMonth = (calendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
            val totalDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val startDayOfWeek = (firstDayOfMonth.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1 // T2=1..CN=7

            Column {
                var day = 1
                for (week in 0..5) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        for (d in 1..7) {
                            if ((week == 0 && d < startDayOfWeek) || day > totalDays) {
                                Box(Modifier.size(40.dp)) {}
                            } else {
                                val cellCal = (calendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, day) }
                                val cellDate = cellCal.time
                                val dateKey = formatter.format(cellDate)
                                val hasNotes = notesByDate[dateKey]?.isNotEmpty() == true

                                val income = transactions.filter {
                                    (it.type.equals("income", true) || it.type.equals("Thu", true)) &&
                                            sameDay(it.date, cellDate, formatter)
                                }.sumOf { it.amount }
                                val expense = transactions.filter {
                                    (it.type.equals("expense", true) || it.type.equals("Chi", true)) &&
                                            sameDay(it.date, cellDate, formatter)
                                }.sumOf { it.amount }

                                val dayTotal = transactions
                                    .filter { it.date == dateKey }
                                    .sumOf { if (it.type.equals("income", true) || it.type.equals("Thu", true)) it.amount else -it.amount }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clickable {
                                            selectedDate = cellDate
                                            if (hasNotes) {
                                                selectedNotes = notesByDate[dateKey] ?: emptyList()
                                                showNotesDialog = true
                                            }
                                        }
                                        .background(
                                            when {
                                                formatter.format(selectedDate) == dateKey -> Color(0xFFEFEFEF)
                                                hasNotes -> Color(0xFFFFCDD2) // Ngày có note
                                                else -> Color.Transparent
                                            }
                                        )
                                ) {
                                    Text(
                                        "$day",
                                        fontSize = 12.sp,
                                        fontWeight = if (hasNotes) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (dayTotal != 0.0) Text(
                                        text = (if (dayTotal > 0) "+" else "-") + formatCurrency(kotlin.math.abs(dayTotal)),
                                        color = if (dayTotal > 0) Color(0xFF004400) else Color(0xFFFF9800),
                                        fontSize = 10.sp
                                    )
                                }

                                day++
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            fun sameDay(d1: Date, d2: Date): Boolean {
                val cal1 = Calendar.getInstance().apply { time = d1 }
                val cal2 = Calendar.getInstance().apply { time = d2 }
                return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                        cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                        cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
            }

            // --- Danh sách giao dịch theo ngày chọn ---
            val filteredTransactions = transactions.filter {
                sameDay(formatter.parse(it.date)!!, selectedDate)
            }


            LazyColumn {
                items(filteredTransactions) { tran ->
                    ListItem(
                        headlineContent = { Text(tran.note.ifEmpty { "Không có ghi chú" }) },
                        supportingContent = { Text(tran.date) },
                        trailingContent = {
                            Text(
                                text = (if (tran.type.equals("income", true) || tran.type.equals("Thu", true)) "+" else "-") +
                                        formatCurrency(tran.amount),
                                color = if (tran.type.equals("income", true) || tran.type.equals("Thu", true)) Color(0xFF004400) else Color(0xFFFF9800),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    // --- Dialog hiển thị note ---
    if (showNotesDialog) {
        AlertDialog(
            onDismissRequest = { showNotesDialog = false },
            title = { Text("Ghi chú ngày ${formatter.format(selectedDate)}") },
            text = {
                Column {
                    selectedNotes.forEach { note ->
                        Text("• ${note.content}")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotesDialog = false }) {
                    Text("Đóng")
                }
            }
        )
    }
}

/** ===== Helpers ===== */
@SuppressLint("SimpleDateFormat")
private fun sameDay(dateStr: String, other: Date, formatter: SimpleDateFormat): Boolean {
    return try {
        val d = formatter.parse(dateStr) ?: return false
        formatter.format(d) == formatter.format(other)
    } catch (_: Exception) {
        false
    }
}

// 🔥 Hàm format số tiền theo kiểu VNĐ có dấu chấm
fun formatCurrency(amount: Double): String {
    val symbols = DecimalFormatSymbols(Locale.getDefault())
    symbols.groupingSeparator = '.'
    val formatter = DecimalFormat("#,###", symbols)
    return formatter.format(amount) + "đ"
}

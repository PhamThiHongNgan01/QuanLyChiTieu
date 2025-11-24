package com.example.quanlychitieu.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quanlychitieu.R
import com.example.quanlychitieu.model.Transaction
import com.google.firebase.database.*
import java.util.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll


//  Hàm format tiền tệ
fun formatCurrency(amount: Number): String {
    val symbols = DecimalFormatSymbols(Locale("vi", "VN"))
    val formatter = DecimalFormat("#,###", symbols)
    return formatter.format(amount) + "đ"
}

@Composable
fun ReportScreen(navController: NavHostController) {
    var transactions by remember { mutableStateOf(listOf<Transaction>()) }
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("QuanLyChiTieu", Context.MODE_PRIVATE)
    val currentUserId = sharedPref.getString("userId", null)

    // Lấy dữ liệu từ Firebase
    LaunchedEffect(Unit) {
        val databaseUrl =
            "https://quanlychitieu-99d33-default-rtdb.asia-southeast1.firebasedatabase.app/"

        if (!currentUserId.isNullOrEmpty()) {
            val dbRef = FirebaseDatabase.getInstance(databaseUrl)
                .getReference("transactions")
                .child(currentUserId!!)

            dbRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Transaction>()
                    for (child in snapshot.children) {
                        val tran = child.getValue(Transaction::class.java)
                        if (tran != null) list.add(tran)
                    }
                    transactions = list
                }

                override fun onCancelled(error: DatabaseError) {
                    println(" Firebase error: ${error.message}")
                }
            })
        } else {
            println("⚠ currentUserId = null -> chưa đăng nhập hoặc SharedPreferences trống")
        }
    }

    ReportContent(transactions = transactions, navController = navController)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportContent(transactions: List<Transaction>, navController: NavHostController) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = tròn, 1 = cột
    var searchQuery by remember { mutableStateOf("") }

    val calendar = Calendar.getInstance()
    val month = calendar.get(Calendar.MONTH) + 1
    val year = calendar.get(Calendar.YEAR)

    // Lấy tất cả giao dịch
    val allTransactions = transactions

// Lọc theo searchQuery nếu cần
    val filteredTransactions = allTransactions.filter { tran ->
        tran.note.contains(searchQuery, ignoreCase = true) ||
                tran.date.contains(searchQuery, ignoreCase = true) ||
                tran.category.contains(searchQuery, ignoreCase = true)
    }

// Tính tổng thu, chi và số dư
    val totalIncome = allTransactions
        .filter { it.type.equals("Thu", true) || it.type.equals("income", true) }
        .sumOf { it.amount }

    val totalExpense = allTransactions
        .filter { it.type.equals("Chi", true) || it.type.equals("expense", true) }
        .sumOf { it.amount }

    val total = totalIncome - totalExpense


    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        TabRow(selectedTabIndex = selectedTab) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = { Text("Biểu đồ tròn") }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = { Text("Biểu đồ cột") }
                            )
                        }
                    }, windowInsets = WindowInsets(0) // loại bỏ khoảng trống phía trên
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Tìm theo ghi chú hoặc ngày") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }
        }
    )  { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp)
        ) {
            // Tổng quan thu chi
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Chi tiêu", fontSize = 14.sp, color = Color.Gray)
                        Text(
                            "-${formatCurrency(totalExpense)}",
                            color = Color(0xFFFF9800),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Thu nhập", fontSize = 14.sp, color = Color.Gray)
                        Text(
                            "+${formatCurrency(totalIncome)}",
                            color = Color(0xFF004400),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Tổng", fontSize = 14.sp, color = Color.Gray)
                        Text(
                            text = (if (total >= 0) "+" else "-") + formatCurrency(
                                kotlin.math.abs(
                                    total
                                )
                            ),
                            color = if (total >= 0) Color(0xFF660033) else Color(0xFFFF9800),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }


                //  Biểu đồ
            item {
                Spacer(Modifier.height(25.dp))

                if (selectedTab == 0) {
                    if (totalIncome > 0 || totalExpense > 0) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 30.dp),
                            horizontalAlignment = Alignment.CenterHorizontally //  căn giữa nội dung trong Column
                        ) {
                            Box(
                                modifier = Modifier.size(220.dp), //  chỉ giữ đúng kích thước tròn
                                contentAlignment = Alignment.Center
                            ) {
                                DonutChart(
                                    data = listOf(totalIncome.toFloat(), totalExpense.toFloat()),
                                    colors = listOf(Color(0xFF004400), Color(0xFFFF9800))
                                )
                                Text("Thu vs Chi", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        DailyBarChart(
                            transactions = transactions,
                        )
                    }
                }
            }



            // ✅ Lịch sử giao dịch (tiêu đề)
            item {
                Spacer(Modifier.height(30.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("📌 Lịch sử chi tiêu", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Image(
                        painter = painterResource(id = R.drawable.ic_edit),
                        contentDescription = "Chỉnh sửa",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { navController.navigate("editReport") }
                    )
                }
            }


            //  Danh sách lịch sử giao dịch
            items(filteredTransactions.sortedByDescending { it.date }) { tran ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(tran.note.ifEmpty { "Không có ghi chú" }, fontWeight = FontWeight.Medium)
                        Text(tran.date, fontSize = 12.sp, color = Color.Gray)
                    }
                    Text(
                        text = (if (tran.type.equals("thu", true)) "+" else "-") + formatCurrency(tran.amount),
                        color = if (tran.type.equals("thu", true)) Color(0xFF004400) else Color(0xFFFF9800),
                        fontWeight = FontWeight.Bold
                    )
                }
                Divider()
            }
        }
    }

}

@Composable
fun DonutChart(data: List<Float>, colors: List<Color>) {
    if (data.isEmpty() || data.sum() == 0f) return
    val sum = data.sum()
    var startAngle = -90f
    Canvas(modifier = Modifier.fillMaxSize()) {
        data.forEachIndexed { index, value ->
            val sweep = 360 * (value / sum)
            drawArc(
                color = colors[index % colors.size],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                size = Size(size.width, size.height),
                style = Stroke(width = size.minDimension / 5)
            )
            startAngle += sweep
        }
    }
}
@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyBarChart(transactions: List<Transaction>) {
    val calendar = Calendar.getInstance()

    var selectedMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH) + 1) }
    var selectedYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }

    //  Tính số ngày trong tháng
    val daysInMonth = remember(selectedMonth, selectedYear) {
        calendar.set(Calendar.YEAR, selectedYear)
        calendar.set(Calendar.MONTH, selectedMonth - 1)
        calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    //  Gom dữ liệu theo ngày
    val dailyData = (1..daysInMonth).map { day ->
        val income = transactions.filter { tran ->
            val parts = tran.date.split("-", "/")
            if (parts.size < 3) return@filter false
            val d = parts[0].toIntOrNull() ?: return@filter false
            val m = parts[1].toIntOrNull() ?: return@filter false
            val y = parts[2].toIntOrNull() ?: return@filter false
            d == day && m == selectedMonth && y == selectedYear &&
                    (tran.type.equals("thu", true) || tran.type.equals("income", true))
        }.sumOf { it.amount }

        val expense = transactions.filter { tran ->
            val parts = tran.date.split("-", "/")
            if (parts.size < 3) return@filter false
            val d = parts[0].toIntOrNull() ?: return@filter false
            val m = parts[1].toIntOrNull() ?: return@filter false
            val y = parts[2].toIntOrNull() ?: return@filter false
            d == day && m == selectedMonth && y == selectedYear &&
                    (tran.type.equals("chi", true) || tran.type.equals("expense", true))
        }.sumOf { it.amount }

        day to (income to expense)
    }

    val maxValue = dailyData.maxOfOrNull { maxOf(it.second.first, it.second.second) }?.toFloat() ?: 1f

    Column(
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        Text("Biểu đồ thu chi theo ngày (Tháng $selectedMonth / $selectedYear)", fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(12.dp))

        //  Bộ lọc tháng + năm
        MonthYearDropdown(
            selectedMonth = selectedMonth,
            onMonthChange = { month -> selectedMonth = month },
            selectedYear = selectedYear,
            onYearChange = { year -> selectedYear = year }
        )

        Spacer(Modifier.height(12.dp))

        //  Biểu đồ từng ngày, có thể kéo ngang
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.Bottom
        ) {
            dailyData.forEach { (day, pair) ->
                val (income, expense) = pair
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height((150f * (income.toFloat() / maxValue)).dp)
                            .background(Color(0xFF004400))
                    )
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height((150f * (expense.toFloat() / maxValue)).dp)
                            .background(Color(0xFFFF9800))
                    )
                    Text(day.toString(), fontSize = 10.sp)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text("🟩  Thu nhập   🟧 Chi tiêu", fontSize = 12.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthYearDropdown(
    selectedMonth: Int,
    onMonthChange: (Int) -> Unit,
    selectedYear: Int,
    onYearChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DropdownSelector(
            label = "Tháng",
            options = (1..12).map { it.toString() },
            selected = selectedMonth.toString(),
            onSelectedChange = { onMonthChange(it.toInt()) },
            modifier = Modifier.weight(1f) // ✅ tự giãn ra ngang
        )

        DropdownSelector(
            label = "Năm",
            options = (2020..2030).map { it.toString() },
            selected = selectedYear.toString(),
            onSelectedChange = { onYearChange(it.toInt()) },
            modifier = Modifier.weight(1f) //  tự giãn ra ngang
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    label: String,
    options: List<String>,
    selected: String,
    onSelectedChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelectedChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}



package com.example.quanlychitieu.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class Category(
    val name: String,
    val icon: Int,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    currentPage: Int,
    onNavigate: (Int) -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var note by remember { mutableStateOf("") }
    var money by remember { mutableStateOf("") }

    Scaffold(

        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Tiền chi") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Tiền thu") })
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Gọi sang file khác
            if (selectedTab == 0) {
                ExpenseScreen(note, money, { note = it }, { money = it })
            } else {
                IncomeScreen(note, money, { note = it }, { money = it })
            }
        }
    }
}

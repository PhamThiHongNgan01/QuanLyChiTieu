package com.example.quanlychitieu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.quanlychitieu.screens.*
import com.example.quanlychitieu.ui.theme.QuanLyChiTieuTheme
import com.example.quanlychitieu.model.Transaction
import com.google.firebase.database.*
import com.google.firebase.FirebaseApp
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import com.example.quanlychitieu.screens.EditReportScreen


class MainActivity : ComponentActivity() {
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        // 🔑 Khởi tạo Firebase
        FirebaseApp.initializeApp(this)
        FirebaseDatabase.getInstance().setLogLevel(com.google.firebase.database.Logger.Level.DEBUG)
        // ✅ Khởi tạo database Firebase
        // URL lấy từ Firebase Console
        val databaseUrl = "https://quanlychitieu-99d33-default-rtdb.asia-southeast1.firebasedatabase.app/"
        // Trỏ đúng database
        database = FirebaseDatabase.getInstance(databaseUrl).getReference("transactions")

        setContent {
            QuanLyChiTieuTheme {
                val navController = rememberNavController()

                // ✅ State chứa danh sách giao dịch
                val transactions = remember { mutableStateListOf<Transaction>() }

                // ✅ Load dữ liệu từ Firebase khi mở app
                LaunchedEffect(Unit) {
                    database.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            transactions.clear()
                            for (child in snapshot.children) {
                                val transaction = child.getValue(Transaction::class.java)
                                transaction?.let { transactions.add(it) }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Log lỗi nếu cần
                        }
                    })
                }

                // Lấy route hiện tại
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // Chỉ hiện bottom bar nếu không nằm trong các route cần ẩn
                        if (currentRoute !in listOf("login", "register")) {
                            NavigationBar {
                                NavigationBarItem(
                                    icon = {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_edit),
                                            contentDescription = "Nhập vào"
                                        )
                                    },
                                    label = { Text("Nhập vào") },
                                    selected = currentRoute == "home",
                                    onClick = { navController.navigate("home") }
                                )
                                NavigationBarItem(
                                    icon = {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_calendar),
                                            contentDescription = "Lịch"
                                        )
                                    },
                                    label = { Text("Lịch") },
                                    selected = currentRoute == "calendar",
                                    onClick = { navController.navigate("calendar") }
                                )
                                NavigationBarItem(
                                    icon = {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_report),
                                            contentDescription = "Báo cáo"
                                        )
                                    },
                                    label = { Text("Báo cáo") },
                                    selected = currentRoute == "report",
                                    onClick = { navController.navigate("report") }
                                )
                                NavigationBarItem(
                                    icon = {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_setting),
                                            contentDescription = "Cài đặt"
                                        )
                                    },
                                    label = { Text("Cài đặt") },
                                    selected = currentRoute == "setting",
                                    onClick = { navController.navigate("setting") }
                                )
                            }
                        }
                    }
                )
                { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("login") {
                            LoginScreen(
                                onNavigateToRegister = { navController.navigate("register") },
                                onLoginSuccess = {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("register") {
                            RegisterScreen(
                                onBackToLogin = { navController.popBackStack() }
                            )
                        }
                        composable("home") {
                            HomeScreen(
                                currentPage = 0,
                                onNavigate = { },
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("calendar") {
                            CalendarScreen(navController = navController)
                        }

                        // 📝 Trang ghi chú
                        composable("ghiChu") {
                            NoteScreen(navController = navController)
                        }
                        composable("report") {
                            ReportScreen(navController = navController)
                        }

                        composable("editReport") {
                            EditReportScreen(navController = navController)
                        }


                        composable("setting") {
                            SettingScreen(navController = navController)

                        }
                        composable("support") {
                            SupportScreen(navController = navController)

                        }
                        composable("account") {
                            val context = LocalContext.current
                            val prefs = context.getSharedPreferences("QuanLyChiTieu", android.content.Context.MODE_PRIVATE)
                            val userId = prefs.getString("userId", null)

                            AccountScreen(
                                userId = userId,
                                onBack = { navController.popBackStack() },
                                onEditPassword = {
                                    userId?.let {
                                        navController.navigate("change_password/$it")  //  truyền userId qua route
                                    }
                                }
                            )
                        }

                        composable("change_password/{userId}") { backStackEntry ->
                            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
                            ChangePasswordScreen(
                                navController = navController,
                                userId = userId
                            )
                        }




                    }
                }
            }
        }
    }
}

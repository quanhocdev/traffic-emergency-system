package com.example.canhbao.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.canhbao.data.network.AppConfig
import com.example.canhbao.ui.screens.*
import com.example.canhbao.viewmodel.*
import ua.naiksoftware.stomp.Stomp

@Composable
fun NavGraph(authViewModel: AuthViewModel) {
    val webrtcViewModel: WebRTCViewModel = viewModel()
    val navController = rememberNavController()
    val context = LocalContext.current

    // 1. Khởi tạo StompClient kết nối đến máy chủ Backend Realtime
    // (Thay thế bằng URL ws chuẩn của bạn nếu AppConfig.WS_BASE_URL chưa định nghĩa cấu trúc /ws/websocket)
    val stompClient = remember {
        val baseUrl = AppConfig.WS_BASE_URL
        val fullWsUrl = if (baseUrl.endsWith("/")) "${baseUrl}ws/websocket" else "$baseUrl/ws/websocket"

        android.util.Log.d("NavGraph", "🔌 Đang kết nối STOMP đến thiết bị thật: $fullWsUrl")
        Stomp.over(Stomp.ConnectionProvider.OKHTTP, fullWsUrl)
    }

    LaunchedEffect(stompClient) {
        try {
            if (!stompClient.isConnected) {
                stompClient.connect()
                android.util.Log.d("NavGraph", "🟢 Đã phát lệnh kết nối STOMP thành công.")
            }
        } catch (e: Exception) {
            android.util.Log.e("NavGraph", "🔴 Lỗi khi cố gắng kết nối STOMP: ${e.message}")
        }
    }

    // 2. Khởi tạo các ViewModel
    val mapViewModel: MapViewModel = viewModel()
    val sosViewModel: SOSViewModel = viewModel()          // Dùng cho MapScreen nhận tín hiệu phản hồi
    val searchViewModel: SearchViewModel = viewModel()    // Dùng cho thanh tìm kiếm vị trí trên map
    val callViewModel: CallViewModel = viewModel()        // Dùng quản lý cuộc gọi đàm thoại cứu hộ

    val tinhHieuSosViewModel: TinHieuSOSViewModel = viewModel() // Giữ nguyên ViewModel cũ của màn hình SOS rời
    val lichSuViewModel: LichSuViewModel = viewModel()
    val apiService = com.example.canhbao.data.network.BaoCaoSuCoRetrofit.api

    // ✅ Khởi tạo GoiViewModel bằng Factory
    val goiViewModel: GoiViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return GoiViewModel(apiService) as T
            }
        }
    )

    val quaViewModel: QuaViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return QuaViewModel(apiService) as T
            }
        }
    )

    // 3. Theo dõi User
    val currentUser by authViewModel.user.collectAsState()

    // Xác định trang bắt đầu
    val startDest = if (currentUser != null) "map" else "login"

    NavHost(navController = navController, startDestination = startDest) {

        composable("login") {
            LoginScreen(viewModel = authViewModel) {
                navController.navigate("map") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }

        // --- ĐÃ SỬA LỖI: Truyền đầy đủ 100% tham số cho MapScreen ---
        composable("map") {
            val alertViewModel: AlertViewModel = viewModel()
            MapScreen(
                navController = navController,
                mapViewModel = mapViewModel,
                webrtcViewModel = webrtcViewModel,
                alertViewModel = alertViewModel,
                searchViewModel = searchViewModel,
                sosViewModel = sosViewModel,
                callViewModel = callViewModel,
                stompClient = stompClient, // <--- HẾT LỖI TẠI ĐÂY
                isLoggedIn = currentUser != null,
                onReportClick = { navController.navigate("bao_cao_su_co") }
            )
        }

        composable("goi_screen") {
            GoiScreen(
                viewModel = goiViewModel,
                navController = navController
            )
        }
        composable("qua_screen") {
            Spacer(modifier = Modifier.height(1.dp)) // Bạn có thể giữ nguyên block QuaScreen cũ
            QuaScreen(
                viewModel = quaViewModel,
                navController = navController
            )
        }

        composable("doitien_screen") {
            val doiTienVM: DoiTienViewModel = viewModel()
            DoiTienScreen(
                navController = navController,
                uid = currentUser?.uid,
                viewModel = doiTienVM
            )
        }

        composable("home") {
            HomeScreen(
                userEmail = currentUser?.email,
                userPhotoUrl = currentUser?.photoUrl?.toString(),
                uid = currentUser?.uid,
                navController = navController,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "tin_hieu_sos/{lat}/{lng}",
            arguments = listOf(
                navArgument("lat") { type = NavType.StringType },
                navArgument("lng") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
            val lng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull() ?: 0.0
            TinHieuSOSScreen(
                navController = navController,
                viewModel = tinhHieuSosViewModel,
                lat = lat,
                lng = lng,
                userId = currentUser?.uid ?: "GUEST"
            )
        }

        composable(
            route = "chi_tiet_hoa_don/{hoaDonId}",
            arguments = listOf(navArgument("hoaDonId") { type = NavType.LongType })
        ) { backStackEntry ->
            ChiTietHoaDonScreen(
                hoaDonId = backStackEntry.arguments?.getLong("hoaDonId") ?: 0L,
                navController = navController
            )
        }

        composable(
            route = "thanh_toan/{hoaDonId}",
            arguments = listOf(navArgument("hoaDonId") { type = NavType.LongType })
        ) { backStackEntry ->
            ThanhToanScreen(
                hoaDonId = backStackEntry.arguments?.getLong("hoaDonId") ?: 0L,
                navController = navController
            )
        }

        composable("tui_screen") {
            TuiScreen(
                viewModel = quaViewModel,
                uid = currentUser?.uid ?: "",
                onBack = { navController.popBackStack() }
            )
        }

        composable("lich_su") {
            LichSuScreen(
                navController = navController,
                baoCaoViewModel = viewModel(),
                tinHieuViewModel = viewModel()
            )
        }

        composable("bao_cao_su_co") {
            BaoCaoSuCoScreen(
                onBack = { navController.popBackStack() },
                navController = navController
            )
        }

        composable("di_chuyen") {
            DiChuyenScreen(mapViewModel = mapViewModel, navController = navController)
        }
    }
}
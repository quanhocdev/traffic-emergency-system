package com.example.canhbao.navigation

import android.util.Log
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.canhbao.data.network.SocketClientProvider
import com.example.canhbao.ui.screens.*
import com.example.canhbao.ui.screens.call.CallScreen
import com.example.canhbao.ui.screens.theodoi.suco.ChiTietBaoCaoScreen
import com.example.canhbao.ui.screens.theodoi.tinhieu.ChiTietSosScreen
import com.example.canhbao.ui.screens.theodoi.LichSuScreen
import com.example.canhbao.ui.screens.theodoi.hoadon.ChiTietHoaDonScreen
import com.example.canhbao.ui.screens.theodoi.hoadon.ThanhToanScreen
import com.example.canhbao.viewmodel.*
import com.example.canhbao.viewmodel.call.CallViewModel
import com.example.canhbao.viewmodel.call.WebRTCViewModel
import com.example.canhbao.viewmodel.goi.GoiViewModel
import com.example.canhbao.viewmodel.helper.AlertViewModel
import com.example.canhbao.viewmodel.helper.SearchViewModel
import com.example.canhbao.viewmodel.suco.TheoDoiBaoCaoViewModel
import com.example.canhbao.viewmodel.tienich.DoiTienViewModel
import com.example.canhbao.viewmodel.tienich.QuaViewModel
import com.example.canhbao.viewmodel.tinhieu.SOSViewModel
import com.example.canhbao.viewmodel.tinhieu.TheoDoiTinHieuViewModel
import com.example.canhbao.viewmodel.tinhieu.TinHieuSOSViewModel
import com.example.canhbao.viewmodel.xacthuc.AuthViewModel
import com.example.canhbao.viewmodel.camera.CameraViewModel
import com.example.canhbao.viewmodel.truso.TruSoViewModel
import ua.naiksoftware.stomp.StompClient

@Composable
fun NavGraph(
    authViewModel: AuthViewModel,
    webrtcViewModel: WebRTCViewModel
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    var stompClient by remember {
        mutableStateOf<StompClient?>(null)
    }

    LaunchedEffect(Unit) {
        stompClient = SocketClientProvider.ensureConnected()
    }
    // Bộ lắng nghe trạng thái cuộc gọi toàn cục
    val callState by webrtcViewModel.callState.collectAsState()

    // Tự động chuyển hướng sang màn hình đàm thoại khi có cuộc gọi tới bất kể đang ở màn hình nào
    LaunchedEffect(callState) {
        if (callState == "INCOMING") {
            navController.navigate("call_screen") {
                launchSingleTop = true // Tránh mở chồng nhiều màn hình khi nhận candidate liên tục
            }
        }
    }

    // Tiến hành phát lệnh kết nối tập trung nếu chưa mở cổng mạng
    LaunchedEffect(stompClient) {
        stompClient?.let { client ->
            try {
                if (!client.isConnected) {
                    client.connect()
                    Log.d("NavGraph", "Đã kết nối STOMP")
                }
            } catch (e: Exception) {
                Log.e("NavGraph", "Lỗi: ${e.message}")
            }
        }
    }

    // Khởi tạo các ViewModel hệ thống
    val mapViewModel: MapViewModel = viewModel()

    val truSoViewModel: TruSoViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TruSoViewModel(context.applicationContext) as T
            }
        }
    )

    val cameraViewModel: CameraViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CameraViewModel(context.applicationContext) as T
            }
        }
    )

    val sosViewModel: SOSViewModel = viewModel()
    val searchViewModel: SearchViewModel = viewModel()
    val callViewModel: CallViewModel = viewModel()
    val tinhHieuSosViewModel: TinHieuSOSViewModel = viewModel()

    val theoDoiBaoCaoViewModel: TheoDoiBaoCaoViewModel = viewModel()
    val theoDoiTinHieuViewModel: TheoDoiTinHieuViewModel = viewModel()

    val apiService = com.example.canhbao.data.network.BaoCaoSuCoRetrofit.api

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

    val currentUser by authViewModel.user.collectAsState()
    val startDest = if (currentUser != null) "map" else "login"

    NavHost(navController = navController, startDestination = startDest) {

        composable("login") {
            LoginScreen(viewModel = authViewModel) {
                navController.navigate("map") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }

        composable("map") {
            val alertViewModel: AlertViewModel = viewModel()
            MapScreen(
                navController = navController,
                mapViewModel = mapViewModel,
                truSoViewModel = truSoViewModel,
                cameraViewModel = cameraViewModel,
                webrtcViewModel = webrtcViewModel,
                alertViewModel = alertViewModel,
                searchViewModel = searchViewModel,
                sosViewModel = sosViewModel,
                isLoggedIn = currentUser != null,
                onReportClick = { navController.navigate("bao_cao_su_co") }
            )
        }

        // Đăng ký màn hình đàm thoại độc lập
        composable("call_screen") {
            CallScreen(
                navController = navController,
                webrtcViewModel = webrtcViewModel
            )
        }

        composable("goi_screen") {
            GoiScreen(viewModel = goiViewModel, navController = navController)
        }

        composable("qua_screen") {
            Spacer(modifier = Modifier.height(1.dp))
            QuaScreen(viewModel = quaViewModel, navController = navController)
        }

        composable("doitien_screen") {
            val doiTienVM: DoiTienViewModel = viewModel()
            DoiTienScreen(navController = navController, uid = currentUser?.uid, viewModel = doiTienVM)
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
            TuiScreen(viewModel = quaViewModel, uid = currentUser?.uid ?: "", onBack = { navController.popBackStack() })
        }

        composable("lich_su") {
            LichSuScreen(navController = navController, baoCaoViewModel = theoDoiBaoCaoViewModel, tinHieuViewModel = theoDoiTinHieuViewModel)
        }

        composable(
            route = "chi_tiet_sos/{sosId}",
            arguments = listOf(navArgument("sosId") { type = NavType.LongType })
        ) { backStackEntry ->
            val sosId = backStackEntry.arguments?.getLong("sosId") ?: 0L
            ChiTietSosScreen(sosId = sosId, navController = navController, viewModel = theoDoiTinHieuViewModel)
        }

        composable(
            route = "chi_tiet_bao_cao/{suCoId}",
            arguments = listOf(navArgument("suCoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val suCoId = backStackEntry.arguments?.getLong("suCoId") ?: 0L
            ChiTietBaoCaoScreen(suCoId = suCoId, navController = navController, viewModel = theoDoiBaoCaoViewModel)
        }

        composable("bao_cao_su_co") {
            BaoCaoSuCoScreen(onBack = { navController.popBackStack() }, navController = navController)
        }

        composable("di_chuyen") {
            DiChuyenScreen(mapViewModel = mapViewModel, navController = navController)
        }
    }
}
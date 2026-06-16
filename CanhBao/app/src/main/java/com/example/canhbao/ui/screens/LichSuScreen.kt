package com.example.canhbao.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.canhbao.ui.components.BaoCaoItemUI
import com.example.canhbao.ui.components.TinHieuItemUI
import com.example.canhbao.viewmodel.TheoDoiBaoCaoUiState
import com.example.canhbao.viewmodel.TheoDoiBaoCaoViewModel
import com.example.canhbao.viewmodel.TheoDoiTinHieuUiState
import com.example.canhbao.viewmodel.TheoDoiTinHieuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LichSuScreen(
    navController: NavController,
    baoCaoViewModel: TheoDoiBaoCaoViewModel,
    tinHieuViewModel: TheoDoiTinHieuViewModel
) {
    val trafficBlue = Color(0xFF1976D2)
    val sosRed = Color(0xFFD32F2F)

    var selectedMainTab by remember { mutableIntStateOf(0) }
    val mainTabs = listOf("SỰ CỐ", "SOS")
    var selectedStatusTab by remember { mutableIntStateOf(0) }

    // 🌟 ĐỒNG BỘ: Danh sách nhãn hiển thị bộ lọc trạng thái trên UI
    val statusFilters = listOf("Đã tiếp nhận", "Chờ xử lý", "Đang xử lý", "Đã xong", "Đã hủy")

    LaunchedEffect(Unit) {
        baoCaoViewModel.fetchData()
        tinHieuViewModel.fetchData()
    }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 4.dp) {
                Column(modifier = Modifier.background(Color.White)) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text("LỊCH SỬ HOẠT ĐỘNG", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = trafficBlue)
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
                    )

                    TabRow(
                        selectedTabIndex = selectedMainTab,
                        containerColor = Color.White,
                        contentColor = if (selectedMainTab == 1) sosRed else trafficBlue,
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedMainTab]),
                                color = if (selectedMainTab == 1) sosRed else trafficBlue,
                                height = 3.dp
                            )
                        }
                    ) {
                        mainTabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedMainTab == index,
                                onClick = { selectedMainTab = index },
                                text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                            )
                        }
                    }

                    ScrollableTabRow(
                        selectedTabIndex = selectedStatusTab,
                        edgePadding = 12.dp,
                        containerColor = Color(0xFFF8F9FA),
                        divider = {},
                        indicator = {}
                    ) {
                        statusFilters.forEachIndexed { index, label ->
                            val isSelected = selectedStatusTab == index
                            val activeColor = if (selectedMainTab == 1) sosRed else trafficBlue
                            Tab(
                                selected = isSelected,
                                onClick = { selectedStatusTab = index },
                                text = {
                                    Surface(
                                        color = if (isSelected) activeColor else Color.Transparent,
                                        shape = RoundedCornerShape(20.dp),
                                        border = if (isSelected) null else BorderStroke(1.dp, Color.LightGray),
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                    ) {
                                        Text(
                                            label,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            fontSize = 12.sp,
                                            color = if (isSelected) Color.White else Color.Gray
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF5F5F5))) {
            if (selectedMainTab == 0) {
                // ================= TAB 1: SỰ CỐ CÔNG CỘNG =================
                when (val state = baoCaoViewModel.uiState) {
                    is TheoDoiBaoCaoUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = trafficBlue)
                    is TheoDoiBaoCaoUiState.Error -> ErrorUI(state.message, Modifier.align(Alignment.Center)) { baoCaoViewModel.fetchData() }
                    is TheoDoiBaoCaoUiState.Success -> {
                        val filtered = state.data.filter { item ->
                            val status = item.trangThaiXuLy?.trim() ?: ""
                            when (selectedStatusTab) {
                                0 -> status == "Đã tiếp nhận"
                                1 -> status == "Chờ xử lý"
                                2 -> status == "Đang xử lý"  // 🎯 Khớp 100% với log "ID: 51 -> Trang thai thuc te: Đang xử lý"
                                3 -> status == "Đã hoàn thành" || status == "Đã xong"
                                4 -> status == "Đã hủy"
                                else -> true
                            }
                        }
                        
                        if (filtered.isEmpty()) {
                            EmptyStateUI(Modifier.align(Alignment.Center))
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 4.dp)) {
                                items(filtered) { item ->
                                    android.util.Log.d("DATA_CHECK", "ID: ${item.id} -> Trang thai thuc te: ${item.trangThaiXuLy}")
                                    // 🚀 ĐÃ CẬP NHẬT: Gọi đúng hàm callback onDetailClick để mở màn hình chi tiết
                                    BaoCaoItemUI(
                                        item = item,
                                        onDetailClick = { suCoId ->
                                            navController.navigate("chi_tiet_bao_cao/$suCoId")
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // ================= TAB 2: CỨU HỘ SOS =================
                when (val state = tinHieuViewModel.uiState) {
                    is TheoDoiTinHieuUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = sosRed)
                    is TheoDoiTinHieuUiState.Error -> ErrorUI(state.message, Modifier.align(Alignment.Center)) { tinHieuViewModel.fetchData() }
                    is TheoDoiTinHieuUiState.Success -> {
                        val filtered = state.data.filter { item ->
                            when (selectedStatusTab) {
                                0 -> item.trangThai == "DA_TIEP_NHAN"
                                1 -> item.trangThai == "CHO_XU_LY" || item.trangThai == "PENDING"
                                2 -> item.trangThai == "DANG_XU_LY"
                                3 -> item.trangThai == "HOAN_THANH"
                                4 -> item.trangThai == "HUY_BO"
                                else -> true
                            }
                        }
                        if (filtered.isEmpty()) { EmptyStateUI(Modifier.align(Alignment.Center)) } else {
                            LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 4.dp)) {
                                items(filtered) { item ->
                                    TinHieuItemUI(
                                        item = item,
                                        onDetailClick = { sosId ->
                                            navController.navigate("chi_tiet_sos/$sosId")
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable fun EmptyStateUI(modifier: Modifier) { Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.DateRange, null, tint = Color.LightGray, modifier = Modifier.size(64.dp)); Text("Không có dữ liệu", color = Color.Gray) } }
@Composable fun ErrorUI(message: String, modifier: Modifier, onRetry: () -> Unit) { Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) { Text("Lỗi: $message", color = Color.Red); Spacer(Modifier.height(8.dp)); Button(onClick = onRetry) { Text("Thử lại") } } }
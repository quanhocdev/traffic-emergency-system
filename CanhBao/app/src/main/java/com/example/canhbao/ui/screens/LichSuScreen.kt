package com.example.canhbao.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import com.example.canhbao.viewmodel.TheoDoiBaoCaoUiState
import com.example.canhbao.viewmodel.TheoDoiTinHieuUiState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.example.canhbao.R
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.canhbao.data.model.hoadon.payment.ThanhToanResponseDTO
import com.example.canhbao.data.model.qua.doiqua.TuiQuaResponseDTO
import com.example.canhbao.ui.components.BaoCaoItemUI
import com.example.canhbao.ui.components.TinHieuItemUI
import com.example.canhbao.viewmodel.TheoDoiBaoCaoViewModel
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

    // State quản lý UI
    var tempSelectedInvoice by remember {
        mutableStateOf<ThanhToanResponseDTO?>(null)
    }
    var selectedMainTab by remember { mutableIntStateOf(0) }
    val mainTabs = listOf("SỰ CỐ", "SOS")
    var selectedStatusTab by remember { mutableIntStateOf(0) }


    val statusFilters = listOf("Chờ tiếp nhận", "Đang xử lý", "Đã xong", "Đã hủy")
    var showPayDialog by remember { mutableStateOf(false) }
    var selectedVoucher by remember { mutableStateOf<TuiQuaResponseDTO?>(null) }

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

                // SỰ CỐ

                when (val state = baoCaoViewModel.uiState) {

                    is TheoDoiBaoCaoUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    is TheoDoiBaoCaoUiState.Success -> {

                        val filtered = state.data.filter { item ->
                            when (selectedStatusTab) {
                                0 -> item.trangThaiXuLy == "CHO_XU_LY"
                                1 -> item.trangThaiXuLy == "DANG_XU_LY"
                                2 -> item.trangThaiXuLy == "HOAN_THANH"
                                3 -> item.trangThaiXuLy == "HUY_BO"
                                else -> true
                            }
                        }

                        LazyColumn {
                            items(filtered) { item ->
                                BaoCaoItemUI(
                                    item = item,
                                    onCancelClick = {
                                        baoCaoViewModel.cancelBaoCao(item.id)
                                    }
                                )
                            }
                        }
                    }
                    is TheoDoiBaoCaoUiState.Error -> {
                        ErrorUI(
                            state.message,
                            Modifier.align(Alignment.Center)
                        ) {
                            baoCaoViewModel.fetchData()
                        }
                    }
                }

            } else {

                // SOS

                when (val state = tinHieuViewModel.uiState) {

                    is TheoDoiTinHieuUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    is TheoDoiTinHieuUiState.Success -> {

                        val filtered = state.data.filter { item ->
                            when (selectedStatusTab) {
                                0 -> item.trangThai == "PENDING"
                                1 -> item.trangThai == "PROCESSING"
                                2 -> item.trangThai == "COMPLETED"
                                3 -> item.trangThai == "CANCELLED"
                                else -> true
                            }
                        }

                        LazyColumn {
                            items(filtered) { item ->
                                TinHieuItemUI(
                                    item = item,
                                    invoice = tinHieuViewModel.pendingInvoicesMap[item.hoaDonId],
                                    isPlaying = tinHieuViewModel.currentlyPlayingId == item.id,
                                    onPlayAudio = {
                                        tinHieuViewModel.playRecording(it, item.id)
                                    },
                                    onCancelClick = {
                                        tinHieuViewModel.cancelSOS(item.id)
                                    },
                                    onPayClick = {
                                        tempSelectedInvoice =
                                            tinHieuViewModel.pendingInvoicesMap[item.hoaDonId]
                                        showPayDialog = true
                                    },
                                    onViewInvoiceDetail = {
                                        item.hoaDonId?.let { hoaDonId ->
                                            navController.navigate(
                                                "chi_tiet_hoa_don/$hoaDonId"
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }

                    is TheoDoiTinHieuUiState.Error -> {
                        ErrorUI(
                            state.message,
                            Modifier.align(Alignment.Center)
                        ) {
                            tinHieuViewModel.fetchData()
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG THANH TOÁN ---
    if (showPayDialog && tempSelectedInvoice != null) {
        val inv = tempSelectedInvoice!!
        val giaGoc = inv.thanhTien?.toDouble() ?: 0.0

        var soTienGiam = 0.0

        selectedVoucher?.let { v ->
            val phanTram = 0.1 // fallback 10% (tạm)
            soTienGiam = giaGoc * phanTram
        }

        val tongThanhToanHienTai = (giaGoc - soTienGiam).coerceAtLeast(0.0)

        AlertDialog(
            onDismissRequest = { showPayDialog = false },
            confirmButton = {
                Button(onClick = {
                    // 4. CẬP NHẬT: Bỏ maThietBi trong confirmPayment
                    val hoaDonId = inv.hoaDonId ?: return@Button

                    tinHieuViewModel.confirmPayment(
                        hoaDonId,
                        selectedVoucher?.quaId
                    )
                    showPayDialog = false
                    selectedVoucher = null
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) {
                    Text("Tôi đã chuyển khoản")
                }
            },
            dismissButton = { TextButton(onClick = { showPayDialog = false }) { Text("Đóng") } },
            title = { Text("Thanh toán cứu hộ", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Quét mã QR để thanh toán", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.qr_goi),
                        contentDescription = null,
                        modifier = Modifier.size(200.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Voucher có thể dùng", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))

                    tinHieuViewModel.listTuiQua.filter { it.loai == "VOUCHER" && (it.soLuong ?: 0) > 0 }.forEach { voucher ->
                        val isSelected = selectedVoucher?.quaId == voucher.quaId
                        OutlinedCard(
                            onClick = { selectedVoucher = if (isSelected) null else voucher },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) Color(0xFFFBC02D) else Color(0xFFE0E0E0))
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFFFE082)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.ConfirmationNumber, null, tint = Color(0xFFF57F17), modifier = Modifier.size(20.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(voucher.tenQua, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Số lượng: ${voucher.soLuong}", fontSize = 11.sp, color = Color(0xFF2E7D32))
                                }
                                RadioButton(selected = isSelected, onClick = null)
                            }
                        }
                    }

                    Divider(Modifier.padding(vertical = 12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tổng thanh toán:", fontWeight = FontWeight.Bold)
                        Text("${String.format("%,d", tongThanhToanHienTai.toLong())} VNĐ", color = Color.Red, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        )
    }
}

@Composable fun EmptyStateUI(modifier: Modifier) { Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.DateRange, null, tint = Color.LightGray, modifier = Modifier.size(64.dp)); Text("Không có dữ liệu", color = Color.Gray) } }
@Composable fun ErrorUI(message: String, modifier: Modifier, onRetry: () -> Unit) { Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) { Text("Lỗi: $message", color = Color.Red); Button(onClick = onRetry) { Text("Thử lại") } } }
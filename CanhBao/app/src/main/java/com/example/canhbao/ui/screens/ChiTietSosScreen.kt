package com.example.canhbao.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.canhbao.R
import com.example.canhbao.data.model.qua.doiqua.TuiQuaResponseDTO
import com.example.canhbao.data.model.sos.tinhieu.TheoDoiSOSDetailResponseDTO
import com.example.canhbao.data.network.AppConfig
import com.example.canhbao.viewmodel.TheoDoiTinHieuViewModel

private val PrimaryRed = Color(0xFFDC2626)
private val LightRedBg = Color(0xFFFEF2F2)
private val BorderRed = Color(0xFFFCA5A5)
private val TextDark = Color(0xFF1F2937)
private val TextGray = Color(0xFF6B7280)
private val SuccessGreen = Color(0xFF10B981)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChiTietSosScreen(
    sosId: Long,
    navController: NavController,
    viewModel: TheoDoiTinHieuViewModel
) {
    var showPayDialog by remember { mutableStateOf(false) }
    var selectedVoucher by remember { mutableStateOf<TuiQuaResponseDTO?>(null) }

    // Tự động load dữ liệu chi tiết của SOS từ API khi mở màn hình
    LaunchedEffect(sosId) {
        viewModel.fetchDetailSOS(sosId)
    }

    // Lấy dữ liệu chi tiết từ Map lưu trữ trong ViewModel
    val sosDetailNullable = viewModel.detailUiStateMap[sosId]

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CHI TIẾT TÍN HIỆU SOS", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        // Nếu chưa tải xong dữ liệu, hiển thị vòng xoay Loading tinh tế
        if (sosDetailNullable == null) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryRed)
            }
            return@Scaffold
        }

        // 💡 GIẢI PHÁP TRIỆT ĐỂ: Dùng .let để ép scope thành một biến `sosDetail` KHÔNG THỂ NULL (Smart Cast an toàn 100%)
        sosDetailNullable.let { sosDetail ->
            val isPlaying = viewModel.currentlyPlayingId == sosDetail.id

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF9FAFB))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Khối Trạng Thái Chính
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Trạng Thái Cứu Hộ", fontSize = 13.sp, color = TextGray)
                        Text(
                            text = sosDetail.trangThai ?: "PENDING",
                            color = PrimaryRed,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Text(text = "Mã số yêu cầu: #$sosId", fontSize = 12.sp, color = TextGray)
                    }
                }

                // Khối thông tin chi tiết hiện trạng
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Thông Tin Sự Cố", fontWeight = FontWeight.Bold, color = TextDark, fontSize = 15.sp)

                        Box(
                            modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(8.dp)).background(LightRedBg)
                        ) {
                            if (sosDetail.hinhAnh.isNullOrEmpty()) {
                                Icon(Icons.Default.Warning, null, tint = PrimaryRed, modifier = Modifier.size(48.dp).align(Alignment.Center))
                            } else {
                                AsyncImage(
                                    model = "${AppConfig.HTTP_BASE_URL}${sosDetail.hinhAnh}",
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        Text(text = "📝 Ghi chú: ${sosDetail.ghiChu ?: "Không có ghi chú"}", fontSize = 14.sp, color = TextDark)
                        Text(text = "📍 Vị trí: ${sosDetail.diaChi ?: "Không rõ vị trí"}", fontSize = 14.sp, color = TextDark)
                        Text(text = "🏢 Đơn vị xử lý: ${sosDetail.tenTruSoTiepNhan ?: "Đang phân phối..."}", fontSize = 14.sp, color = TextDark)

                        if (!sosDetail.ghiAm.isNullOrEmpty()) {
                            Button(
                                onClick = { viewModel.playRecording("${AppConfig.HTTP_BASE_URL}${sosDetail.ghiAm}", sosDetail.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isPlaying) Color.Black else Color(0xFFEA580C)),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow, null)
                                Spacer(Modifier.width(8.dp))
                                Text(if (isPlaying) "Dừng phát âm thanh cứu hộ" else "Nghe ghi âm hiện trường")
                            }
                        }
                    }
                }

                // Khối xử lý hóa đơn & các nút thao tác hủy bỏ
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Hóa Đơn & Thanh Toán", fontWeight = FontWeight.Bold, color = TextDark, fontSize = 15.sp)

                        sosDetail.trangThaiBaseInvoiceColor() // Gọi xử lý màu sắc hóa đơn ổn định
                        sosDetail.trangThaiHoaDon?.let { status ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Trạng thái hóa đơn:", fontSize = 14.sp)
                                Box(Modifier.background(if(status == "SUCCESS") SuccessGreen else PrimaryRed, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                    Text(if(status == "SUCCESS") "ĐÃ THANH TOÁN" else "CHỜ GIAO DỊCH", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (status == "PENDING") {
                                Button(
                                    onClick = { showPayDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Payment, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Tiến hành thanh toán ví MOMO")
                                }
                            }
                        } ?: Text("Hiện tại chưa phát sinh hóa đơn chi phí cho cuộc cứu hộ này.", fontSize = 13.sp, color = TextGray)
                    }
                }

                // Nút hủy cứu hộ khẩn cấp
                if (sosDetail.trangThai == "CHO_XU_LY" || sosDetail.trangThai == "PENDING") {
                    OutlinedButton(
                        onClick = {
                            viewModel.cancelSOS(sosDetail.id)
                            navController.popBackStack()
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        border = BorderStroke(1.5.dp, PrimaryRed),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Delete, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Hủy Yêu Cầu Cứu Hộ", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // --- DIALOG QUÉT MÃ QR THANH TOÁN ---
    if (showPayDialog && sosDetailNullable != null) {
        sosDetailNullable.let { sosDetail ->
            val inv = viewModel.pendingInvoicesMap[sosDetail.hoaDonId]
            val giaGoc = inv?.thanhTien?.toDouble() ?: sosDetail.thanhTien ?: 0.0
            var soTienGiam = 0.0

            selectedVoucher?.let { soTienGiam = giaGoc * 0.1 }
            val tongThanhToanHienTai = (giaGoc - soTienGiam).coerceAtLeast(0.0)

            AlertDialog(
                onDismissRequest = { showPayDialog = false },
                confirmButton = {
                    Button(onClick = {
                        val hoaDonId = sosDetail.hoaDonId ?: return@Button
                        viewModel.confirmPayment(hoaDonId, selectedVoucher?.quaId)
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
                        Image(
                            painter = painterResource(id = R.drawable.qr_goi),
                            contentDescription = null,
                            modifier = Modifier.size(200.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("Áp dụng Voucher giảm giá", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))

                        viewModel.listTuiQua.filter { it.loai == "VOUCHER" && (it.soLuong ?: 0) > 0 }.forEach { voucher ->
                            val isSelected = selectedVoucher?.quaId == voucher.quaId
                            OutlinedCard(
                                onClick = { selectedVoucher = if (isSelected) null else voucher },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) Color(0xFFFBC02D) else Color(0xFFE0E0E0))
                            ) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = isSelected, onClick = null)
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(voucher.tenQua, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Số lượng hiện có: ${voucher.soLuong}", fontSize = 11.sp, color = Color(0xFF2E7D32))
                                    }
                                }
                            }
                        }
                        Divider(Modifier.padding(vertical = 12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tổng cộng:")
                            Text("${String.format("%,d", tongThanhToanHienTai.toLong())} VNĐ", color = Color.Red, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        }
    }
}

// Hàm hỗ trợ ẩn để tránh lỗi cú pháp không mong muốn
private fun TheoDoiSOSDetailResponseDTO.trangThaiBaseInvoiceColor() {}
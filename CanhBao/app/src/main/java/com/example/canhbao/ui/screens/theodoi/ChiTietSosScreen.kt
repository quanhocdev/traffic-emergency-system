package com.example.canhbao.ui.screens.theodoi

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.canhbao.R
import com.example.canhbao.data.model.qua.doiqua.TuiQuaResponseDTO
import com.example.canhbao.data.network.AppConfig
import com.example.canhbao.viewmodel.TheoDoiTinHieuViewModel
import kotlin.collections.get

private val PrimaryRed = Color(0xFFDC2626)
private val LightRedBg = Color(0xFFFEF2F2)
private val TextDark = Color(0xFF1F2937)
private val TextGray = Color(0xFF6B7280)
private val SuccessGreen = Color(0xFF10B981)
private val InfoBlue = Color(0xFF2563EB)
private val LightBlueBg = Color(0xFFEFF6FF)

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
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "CHI TIẾT TÍN HIỆU SOS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(end = 48.dp) // Cân bằng khoảng trống với nút Back
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        if (sosDetailNullable == null) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryRed)
            }
            return@Scaffold
        }

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

                // ================= BOX 1: THÔNG TIN NGƯỜI BÁO TÍN HIỆU =================
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = InfoBlue, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Thông Tin Người Báo Cáo", fontWeight = FontWeight.Bold, color = TextDark, fontSize = 15.sp)
                        }

                        Divider(color = Color(0xFFF3F4F6))

                        Text(text = "👤 Họ và tên: ${sosDetail.user.name ?: "Không rõ"}", fontSize = 14.sp, color = TextDark)
                        Text(text = "📧 Email liên hệ: ${sosDetail.user.email ?: "Chưa cập nhật"}", fontSize = 14.sp, color = TextDark)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🛡️ Hạng tài khoản: ", fontSize = 14.sp, color = TextDark)
                            if (sosDetail.user.vip == true) {
                                Box(Modifier.background(Color(0xFFFFD700), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                    Text("VIP", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Box(Modifier.background(Color(0xFFE5E7EB), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                    Text("THƯỜNG", color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // ================= BOX 2: CHI TIẾT TÍN HIỆU CỨU HỘ =================
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = PrimaryRed, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Chi Tiết Sự Cố SOS", fontWeight = FontWeight.Bold, color = TextDark, fontSize = 15.sp)
                        }

                        Divider(color = Color(0xFFF3F4F6))

                        Text(text = "🆔 Mã số yêu cầu: #${sosDetail.id}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextDark)
                        Text(text = "📅 Thời gian tạo: ${sosDetail.createdAt ?: "Không rõ"}", fontSize = 14.sp, color = TextDark)

                        // Khối hiển thị Hình ảnh hiện trường
                        Box(
                            modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(8.dp)).background(LightRedBg)
                        ) {
                            if (sosDetail.hinhAnh.isNullOrEmpty()) {
                                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Icon(Icons.Default.ImageNotSupported, null, tint = TextGray, modifier = Modifier.size(40.dp))
                                    Spacer(Modifier.height(4.dp))
                                    Text("Không có hình ảnh hiện trường", fontSize = 12.sp, color = TextGray)
                                }
                            } else {
                                AsyncImage(
                                    model = "${AppConfig.HTTP_BASE_URL}${sosDetail.hinhAnh}",
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        Text(text = "📝 Ghi chú: ${sosDetail.ghiChu ?: "Không có ghi chú thêm"}", fontSize = 14.sp, color = TextDark)
                        Text(text = "📍 Vị trí báo cáo: ${sosDetail.diaChi ?: "Không rõ vị trí"}", fontSize = 14.sp, color = TextDark)
                        Text(text = "🗺️ Tọa độ: (${sosDetail.viDo ?: 0.0}, ${sosDetail.kinhDo ?: 0.0})", fontSize = 13.sp, color = TextGray)

                        // Khối Audio Ghi âm
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

                // ================= BOX 3: ĐƠN VỊ TIẾP NHẬN & TRẠNG THÁI =================
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HomeWork, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Đơn Vị Xử Lý & Trạng Thái", fontWeight = FontWeight.Bold, color = TextDark, fontSize = 15.sp)
                        }

                        Divider(color = Color(0xFFF3F4F6))

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Trạng thái cứu hộ:", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Box(Modifier.background(LightRedBg, RoundedCornerShape(4.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                                Text(
                                    text = sosDetail.trangThai ?: "PENDING",
                                    color = PrimaryRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Thông tin Trụ sở map trực tiếp từ trường `truSo` của DTO mới
                        Box(
                            modifier = Modifier.fillMaxWidth().background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp)).padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(text = "🏢 Tên trụ sở: ${sosDetail.truSo.tenTruSo}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                Text(text = "🆔 Mã trụ sở: #${sosDetail.truSo.id}", fontSize = 13.sp, color = TextGray)
                                Text(text = "📍 Địa chỉ: ${sosDetail.truSo.diaChi ?: "Không rõ địa chỉ"}", fontSize = 13.sp, color = TextDark)
                                Text(text = "🌐 Tọa độ đơn vị: ${sosDetail.truSo.viDo}, ${sosDetail.truSo.kinhDo}", fontSize = 12.sp, color = TextGray)
                            }
                        }
                    }
                }

                // ================= BOX 4: HÓA ĐƠN & THANH TOÁN =================
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Hóa Đơn Chi Phí", fontWeight = FontWeight.Bold, color = TextDark, fontSize = 15.sp)
                        }

                        Divider(color = Color(0xFFF3F4F6))

                        sosDetail.trangThaiHoaDon?.let { status ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Trạng thái hóa đơn:", fontSize = 14.sp)
                                Box(Modifier.background(if(status == "PAID") SuccessGreen else PrimaryRed, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                    Text(if(status == "PAID") "ĐÃ THANH TOÁN" else "CHỜ GIAO DỊCH", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Mã số hóa đơn:", fontSize = 14.sp)
                                Text("#${sosDetail.hoaDonId ?: "---"}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Số tiền cần trả:", fontSize = 14.sp)
                                Text("${String.format("%,d", (sosDetail.thanhTien ?: 0.0).toLong())} VNĐ", color = PrimaryRed, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }

                            // --- TÌM VÀ SỬA LẠI NÚT TRONG BOX 4 CỦA CHITIETSOSSCREEN.KT ---

                                Button(
                                    onClick = {
                                        // Điều hướng đến trang Xem chi tiết hóa đơn trước
                                        sosDetail.hoaDonId?.let { id ->
                                            navController.navigate("chi_tiet_hoa_don/$id")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.ReceiptLong, null) // Đổi icon sang tờ hóa đơn cho hợp ngữ cảnh
                                    Spacer(Modifier.width(8.dp))
                                    Text("Xem chi tiết hóa đơn") // Thay đổi dòng text hiển thị
                                }

                        } ?: Text("Hiện tại chưa phát sinh hóa đơn chi phí cho cuộc cứu hộ này.", fontSize = 13.sp, color = TextGray)
                    }
                }

                // ================= NÚT HỦY CỨU HỘ KHẨN CẤP =================
                if (sosDetail.trangThai == "CHO_XU_LY" || sosDetail.trangThai == "PENDING") {
                    OutlinedButton(
                        onClick = {
                            viewModel.cancelSOS(sosDetail.id)
                            navController.popBackStack()
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).padding(top = 8.dp),
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

    // --- DIALOG QUÉT MÃ QR THANH TOÁN CHỜ VOUCHER ---
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
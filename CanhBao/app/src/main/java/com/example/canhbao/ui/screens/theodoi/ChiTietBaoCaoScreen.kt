package com.example.canhbao.ui.screens.theodoi

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.canhbao.data.network.AppConfig
import com.example.canhbao.viewmodel.TheoDoiBaoCaoViewModel // Giả định tên ViewModel quản lý Báo cáo của bạn

// Định nghĩa bảng màu Xanh nước biển chủ đạo cho phần Chi tiết Báo cáo Sự cố
private val PrimaryBlue = Color(0xFF1976D2)
private val LightBlueBg = Color(0xFFE3F2FD)
private val TextDark = Color(0xFF1F2937)
private val TextGray = Color(0xFF6B7280)
private val SuccessGreen = Color(0xFF10B981)
private val InfoBlue = Color(0xFF2563EB)
private val CancelRed = Color(0xFFDC2626)
private val LightRedBg = Color(0xFFFEF2F2)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChiTietBaoCaoScreen(
    suCoId: Long,
    navController: NavController,
    viewModel: TheoDoiBaoCaoViewModel // Đã chuyển sang dùng ViewModel của luồng Sự cố
) {
    // Tự động load dữ liệu chi tiết của Sự cố từ API khi mở màn hình
    LaunchedEffect(suCoId) {
        viewModel.fetchDetailSuCo(suCoId) // Tên hàm giả định, cấu trúc i chang SOS
    }

    // Lấy dữ liệu chi tiết từ Map lưu trữ trong ViewModel sự cố
    val suCoDetailNullable = viewModel.detailUiStateMap[suCoId]

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "CHI TIẾT BÁO CÁO SỰ CỐ",
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
        if (suCoDetailNullable == null) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
            return@Scaffold
        }

        suCoDetailNullable.let { suCoDetail ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF9FAFB))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ================= BOX 1: THÔNG TIN NGƯỜI BÁO CÁO SỰ CỐ =================
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

                        // Khớp an toàn với đối tượng user? (UserInfoResponseDTO) bên trong DTO Chi tiết sự cố
                        Text(text = "👤 Họ và tên: ${suCoDetail.user?.name ?: "Không rõ"}", fontSize = 14.sp, color = TextDark)
                        Text(text = "📧 Email liên hệ: ${suCoDetail.user?.email ?: "Chưa cập nhật"}", fontSize = 14.sp, color = TextDark)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🛡️ Hạng tài khoản: ", fontSize = 14.sp, color = TextDark)
                            if (suCoDetail.user?.vip == true) {
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

                // ================= BOX 2: CHI TIẾT SỰ CỐ CÔNG CỘNG =================
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Chi Tiết Sự Cố", fontWeight = FontWeight.Bold, color = TextDark, fontSize = 15.sp)
                        }

                        Divider(color = Color(0xFFF3F4F6))

                        Text(text = "🆔 Mã số yêu cầu: #${suCoDetail.id}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextDark)
                        Text(text = "🛠️ Loại sự cố: ${suCoDetail.tenLoai ?: "Không rõ"}", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextDark)
                        Text(text = "📅 Thời gian tạo: ${suCoDetail.thoiGianTao ?: "Không rõ"}", fontSize = 14.sp, color = TextDark)

                        // Khối hiển thị Hình ảnh hiện trường sự cố
                        Box(
                            modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(8.dp)).background(LightBlueBg)
                        ) {
                            if (suCoDetail.hinhAnhUrl.isNullOrEmpty()) {
                                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Icon(Icons.Default.ImageNotSupported, null, tint = TextGray, modifier = Modifier.size(40.dp))
                                    Spacer(Modifier.height(4.dp))
                                    Text("Không có hình ảnh hiện trường", fontSize = 12.sp, color = TextGray)
                                }
                            } else {
                                AsyncImage(
                                    model = "${AppConfig.HTTP_BASE_URL}${suCoDetail.hinhAnhUrl}",
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        Text(text = "📝 Mô tả: ${suCoDetail.moTa ?: "Không có mô tả thêm"}", fontSize = 14.sp, color = TextDark)
                        Text(text = "📍 Vị trí báo cáo: ${suCoDetail.diaChi ?: "Không rõ vị trí"}", fontSize = 14.sp, color = TextDark)

                        // Khối hiển thị Đặc thù đánh giá phân loại của luồng Sự cố (Độ tin cậy & Mức độ nghiêm trọng)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFF0FDF4), shape = RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "🎯 Độ tin cậy: ${suCoDetail.doTinCay ?: "---"}",
                                    color = Color(0xFF16A34A),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(LightBlueBg, shape = RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "🔥 Mức độ: ${suCoDetail.mucDoNghiemTrong ?: "---"}",
                                    color = PrimaryBlue,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // ================= BOX 3: TRẠM TIẾP NHẬN & TRẠNG THÁI XỬ LÝ =================
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
                            Text("Trạng thái xử lý:", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (suCoDetail.trangThaiXuLy == "DA_XU_LY") Color(0xFFF0FDF4) else LightRedBg,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = suCoDetail.trangThaiXuLy ?: "CHO_XU_LY",
                                    color = if (suCoDetail.trangThaiXuLy == "DA_XU_LY") SuccessGreen else CancelRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Hiển thị Thông tin chi tiết Trụ sở map từ đối tượng `truSo?` (TruSoMapDto) của DTO mới
                        if (suCoDetail.truSo != null) {
                            Box(
                                modifier = Modifier.fillMaxWidth().background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp)).padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(text = "🏢 Tên trụ sở: ${suCoDetail.truSo.tenTruSo}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                    Text(text = "🆔 Mã trụ sở: #${suCoDetail.truSo.id}", fontSize = 13.sp, color = TextGray)
                                    Text(text = "📍 Địa chỉ: ${suCoDetail.truSo.diaChi ?: "Không rõ địa chỉ"}", fontSize = 13.sp, color = TextDark)
                                    Text(text = "🌐 Tọa độ đơn vị: ${suCoDetail.truSo.viDo}, ${suCoDetail.truSo.kinhDo}", fontSize = 12.sp, color = TextGray)
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier.fillMaxWidth().background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp)).padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🏢 Đang tìm kiếm đơn vị tiếp nhận quản lý...", fontSize = 13.sp, color = TextGray)
                            }
                        }
                    }
                }
            }
        }
    }
}
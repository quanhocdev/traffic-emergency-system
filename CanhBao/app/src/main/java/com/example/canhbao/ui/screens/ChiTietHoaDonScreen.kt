package com.example.canhbao.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.canhbao.viewmodel.hoadon.ChiTietHoaDonViewModel

// Thống nhất bảng màu Đỏ - Trắng chủ đạo
private val PrimaryRed = Color(0xFFDC2626)
private val LightRedBg = Color(0xFFFEF2F2)
private val BorderRed = Color(0xFFFCA5A5)
private val TextDark = Color(0xFF1F2937)
private val TextGray = Color(0xFF4B5563)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChiTietHoaDonScreen(
    hoaDonId: Long,
    navController: NavController,
    viewModel: ChiTietHoaDonViewModel = viewModel(),
) {
    val hoaDon by remember {
        derivedStateOf { viewModel.hoaDon }
    }

    LaunchedEffect(hoaDonId) {
        viewModel.loadHoaDonDetail(hoaDonId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Chi tiết hóa đơn",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            modifier = Modifier.padding(end = 48.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryRed
                )
            )
        },
        containerColor = Color(0xFFF9FAFB) // Nền xám nhạt nhẹ để làm nổi bật Card trắng
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                // --- TRẠNG THÁI LOADING ---
                viewModel.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryRed)
                    }
                }

                // --- TRẠNG THÁI LỖI ---
                viewModel.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = viewModel.errorMessage ?: "Đã xảy ra lỗi không xác định",
                            color = PrimaryRed,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // --- HIỂN THỊ CHI TIẾT HÓA ĐƠN ---
                hoaDon != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {

                        // Khối thông tin chi tiết
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                // Tiêu đề phụ bên trong Card
                                Text(
                                    text = "THÔNG TIN ĐƠN HÀNG",
                                    color = PrimaryRed,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Danh sách các dòng thông tin dạng Key - Value
                                InfoRow(label = "Mã hóa đơn", value = "#${hoaDon!!.id}")
                                InfoRow(label = "Mã trụ sở cứu hộ", value = "${hoaDon!!.truSo?.id}")
                                InfoRow(label = "Tên trụ sở cứu hộ", value = "${hoaDon!!.truSo?.tenTruSo}")
                                InfoRow(label = "Tên người dùng", value = hoaDon!!.user?.name ?: "N/A")
                                InfoRow(label = "Email", value = hoaDon!!.user?.email ?: "N/A")
                                InfoRow(label = "Nội dung xử lý", value = hoaDon!!.noiDungXuLy ?: "Không có")
                                InfoRow(
                                    label = "Chi phí",
                                    value = "${hoaDon!!.thanhTien}",
                                    valueColor = PrimaryRed,
                                    isBoldValue = true
                                )
                                InfoRow(label = "Ngày tạo", value = hoaDon!!.createdAt ?: "")

                                Spacer(modifier = Modifier.height(12.dp))

                                // Dòng hiển thị Trạng thái (được bọc Badge màu sắc rõ ràng)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Trạng thái", color = TextGray, fontSize = 14.sp)

                                    val isSuccess = hoaDon!!.trangThai == "PAID"
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = if (isSuccess) Color(0xFFD1FAE5) else LightRedBg,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Info,
                                                contentDescription = null,
                                                tint = if (isSuccess) Color(0xFF059669) else PrimaryRed,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isSuccess) "Đã thanh toán" else "Chờ thanh toán",
                                                color = if (isSuccess) Color(0xFF059669) else PrimaryRed,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Khối xử lý Nút bấm hành động cố định ở phía dưới cùng
                        Column(
                            modifier = Modifier.padding(top = 32.dp)
                        ) {
                            when (hoaDon!!.trangThai) {
                                "PENDING" -> {
                                    Button(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp),
                                        onClick = {
                                            navController.navigate("thanh_toan/${hoaDon!!.id}")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = "Tiến hành thanh toán",
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                "SUCCESS" -> {
                                    Button(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp),
                                        onClick = {
                                            navController.navigate("chi_tiet_thanh_toan/${hoaDon!!.id}")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = "Xem chi tiết thanh toán",
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
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
}

// Composable phụ trợ để render từng dòng thông tin sạch sẽ hơn
@Composable
fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = TextDark,
    isBoldValue: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            color = TextGray,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 14.sp,
            fontWeight = if (isBoldValue) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.5f)
        )
    }
}
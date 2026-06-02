package com.example.canhbao.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.canhbao.data.model.qua.doiqua.TuiQuaResponseDTO
import com.example.canhbao.viewmodel.ThanhToanViewModel

// Định nghĩa bảng màu Đỏ - Trắng chủ đạo
private val PrimaryRed = Color(0xFFDC2626)
private val LightRedBg = Color(0xFFFEF2F2)
private val BorderRed = Color(0xFFFCA5A5)
private val TextDark = Color(0xFF1F2937)
private val TextGray = Color(0xFF4B5563)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThanhToanScreen(
    hoaDonId: Long,
    navController: NavController,
    viewModel: ThanhToanViewModel = viewModel()
) {
    var selectedVoucher by remember { mutableStateOf<TuiQuaResponseDTO?>(null) }
    var paymentMethod by remember { mutableStateOf("MOMO") }

    LaunchedEffect(Unit) {
        viewModel.loadVoucher()
    }

    LaunchedEffect(Unit) {
        snapshotFlow { viewModel.paymentSuccess }
            .collect { success ->
                if (success) {
                    viewModel.resetPayment()
                    navController.navigate("chi_tiet_hoa_don/$hoaDonId") {
                        popUpTo("thanh_toan/$hoaDonId") { inclusive = true }
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Thanh toán hóa đơn",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            modifier = Modifier.padding(end = 48.dp) // Cân bằng khoảng trống của nút back
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
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Phần nội dung chính nằm trên
            Column {
                // --- MỤC VOUCHER GIẢM GIÁ ---
                Text(
                    text = "Voucher giảm giá",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                )

                Spacer(Modifier.height(10.dp))

                val vouchers = viewModel.listVoucher.filter {
                    it.loai == "VOUCHER" && (it.soLuong ?: 0) > 0
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    // 1. Render danh sách voucher nếu có
                    items(vouchers) { voucher ->
                        val selected = selectedVoucher?.quaId == voucher.quaId

                        Card(
                            onClick = {
                                selectedVoucher = if (selected) null else voucher
                            },
                            modifier = Modifier.size(width = 150.dp, height = 90.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) LightRedBg else Color.White
                            ),
                            border = BorderStroke(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) PrimaryRed else Color(0xFFE5E7EB)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = voucher.tenQua,
                                    color = TextDark,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selected,
                                        onClick = null,
                                        colors = RadioButtonDefaults.colors(selectedColor = PrimaryRed)
                                    )
                                }
                            }
                        }
                    }

                    // 2. NÚT ĐỔI QUÀ LUÔN LUÔN HIỆN
                    item {
                        Card(
                            modifier = Modifier.size(width = 90.dp, height = 90.dp),
                            shape = RoundedCornerShape(12.dp),
                            onClick = { navController.navigate("qua_screen") },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = PrimaryRed,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Đổi quà",
                                    color = PrimaryRed,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                // --- MỤC PHƯƠNG THỨC THANH TOÁN ---
                Text(
                    text = "Phương thức thanh toán",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                )

                Spacer(Modifier.height(12.dp))

                // Danh sách các Phương thức
                val methods = listOf(
                    "MOMO" to "Ví điện tử MoMo",
                    "BANKING" to "Chuyển khoản ngân hàng",
                    "TIEN_MAT" to "Tiền mặt"
                )

                methods.forEach { (code, name) ->
                    val isSelected = paymentMethod == code
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .background(
                                color = if (isSelected) LightRedBg else Color(0xFFF9FAFB),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { paymentMethod = code }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { paymentMethod = code },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = PrimaryRed,
                                unselectedColor = Color.Gray
                            )
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = name,
                            color = if (isSelected) PrimaryRed else TextDark,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 15.sp
                        )
                    }
                }

                // --- HIỂN THỊ TRẠNG THÁI ---
                Spacer(Modifier.height(20.dp))

                if (viewModel.loading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryRed)
                    }
                }

                if (viewModel.errorMessage != null) {
                    Text(
                        text = viewModel.errorMessage!!,
                        color = PrimaryRed,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                if (viewModel.paymentSuccess) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Thanh toán thành công ✅",
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Phần nút bấm xác nhận cố định ở phía dưới cùng
            Column(
                modifier = Modifier.padding(top = 32.dp)
            ) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !viewModel.loading,
                    onClick = {
                        viewModel.thanhToan(
                            hoaDonId = hoaDonId,
                            quaId = selectedVoucher?.quaId,
                            phuongThuc = paymentMethod
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryRed,
                        disabledContainerColor = BorderRed
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (viewModel.loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp,
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "Xác nhận thanh toán",
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
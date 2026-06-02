package com.example.canhbao.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.canhbao.data.model.suco.baocao.TheoDoiBaoCaoResponseDTO
import com.example.canhbao.data.network.AppConfig

// Định nghĩa bảng màu Xanh nước biển chủ đạo cho phần Báo cáo
private val PrimaryBlue = Color(0xFF1976D2)
private val LightBlueBg = Color(0xFFE3F2FD)
private val BorderBlue = Color(0xFF90CAF9)
private val TextDark = Color(0xFF1F2937)
private val TextGray = Color(0xFF6B7280)
private val CancelRed = Color(0xFFDC2626)

@Composable
fun BaoCaoItemUI(
    item: TheoDoiBaoCaoResponseDTO,
    onCancelClick: () -> Unit
) {
    val displayDate = try {
        item.thoiGianTao
            ?.substringBefore("T")
            ?.split("-")
            ?.reversed()
            ?.joinToString("/")
            ?: ""
    } catch (_: Exception) {
        ""
    }

    // Sử dụng OutlinedCard kèm viền xanh để phân tách rõ ràng giống trang SOS
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp), // Khoảng cách giãn giữa các item
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, BorderBlue), // Viền xanh nước biển bo góc
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // --- PHẦN 1: NỘI DUNG CHÍNH (ẢNH & THÔNG TIN SƠ BỘ) ---
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(85.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(LightBlueBg)
                ) {
                    if (item.hinhAnhUrl.isNullOrEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(32.dp),
                            tint = PrimaryBlue
                        )
                    } else {
                        AsyncImage(
                            model = "${AppConfig.HTTP_BASE_URL}${item.hinhAnhUrl}",
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.tenLoai ?: "Sự cố",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PrimaryBlue
                    )

                    item.moTa?.let {
                        Text(
                            text = it,
                            fontSize = 13.sp,
                            color = TextDark,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = PrimaryBlue
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = item.diaChi ?: "Không rõ vị trí",
                            fontSize = 12.sp,
                            color = TextGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = TextGray
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = displayDate,
                            fontSize = 12.sp,
                            color = TextGray
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Divider(color = Color(0xFFF3F4F6), thickness = 1.dp) // Đường phân cách mờ
            Spacer(Modifier.height(12.dp))

            // --- PHẦN 2: TRẠNG THÁI & CHỈ SỐ (THIẾT KẾ DẠNG BADGE GỌN GÀNG) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFF0FDF4), shape = RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Độ tin cậy: ${item.doTinCay ?: "---"}",
                        color = Color(0xFF16A34A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFFEFF6FF), shape = RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Mức độ: ${item.mucDoNghiemTrong ?: "---"}",
                        color = PrimaryBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Các dòng thông số chi tiết dưới dạng văn bản phối màu nhẹ
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Tiếp nhận: ${item.tenTruSoTiepNhan ?: "Chưa có"}",
                    fontSize = 12.sp,
                    color = TextGray
                )
            }

            // --- PHẦN 3: NÚT BẤM HÀNH ĐỘNG (NẾU CÓ) ---
            if (item.trangThaiXuLy == "CHO_XU_LY" && item.trangThaiDuyet == "AI_APPROVED") {
                Spacer(Modifier.height(14.dp))

                OutlinedButton(
                    onClick = onCancelClick,
                    border = BorderStroke(1.2.dp, CancelRed),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CancelRed),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                ) {
                    Text(
                        text = "Hủy báo cáo",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
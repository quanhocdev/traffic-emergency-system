package com.example.canhbao.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
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
import com.example.canhbao.data.model.sos.tinhieu.TheoDoiSOSDetailResponseDTO
import com.example.canhbao.data.network.AppConfig

// Thống nhất bảng màu Đỏ - Trắng chủ đạo của hệ thống CanhBao
private val PrimaryRed = Color(0xFFDC2626)
private val LightRedBg = Color(0xFFFEF2F2)
private val BorderRed = Color(0xFFFCA5A5)
private val TextDark = Color(0xFF1F2937)
private val TextGray = Color(0xFF6B7280)
private val SuccessGreen = Color(0xFF10B981)

@Composable
fun TinHieuItemUI(
    item: TheoDoiSOSDetailResponseDTO,
    isPlaying: Boolean,
    onPlayAudio: (String) -> Unit,
    onCancelClick: () -> Unit,
    onPayClick: () -> Unit,
    onViewInvoiceDetail: () -> Unit
) {
    // Sử dụng OutlinedCard kèm viền đỏ nhạt để tách biệt rõ ràng từng phần tử SOS
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp), // Tạo độ giãn cách, tránh dính sát nhau giữa các item
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, BorderRed), // Đường viền đỏ mảnh làm nổi bật tính chất Cảnh báo/SOS
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // --- PHẦN NỘI DUNG (ẢNH & THÔNG TIN) ---
            Row(
                verticalAlignment = Alignment.Top
            ) {
                // Ô hiển thị hình ảnh sự cố
                Box(
                    modifier = Modifier
                        .size(85.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(LightRedBg)
                ) {
                    if (item.hinhAnh.isNullOrEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = PrimaryRed,
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.Center)
                        )
                    } else {
                        AsyncImage(
                            model = "${AppConfig.HTTP_BASE_URL}${item.hinhAnh}",
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(Modifier.width(14.dp))

                // Khối Text thông tin
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.ghiChu ?: "Không có ghi chú",
                        color = TextDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = PrimaryRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = item.diaChi ?: "Không rõ vị trí",
                            color = TextGray,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "Tiếp nhận: ${item.tenTruSoTiepNhan ?: "Chưa có"}",
                        color = TextDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Divider(color = Color(0xFFF3F4F6), thickness = 1.dp) // Thanh gạch ngang mờ phân cách giữa nội dung và nút bấm
            Spacer(Modifier.height(12.dp))

            // --- PHẦN NÚT BẤM HÀNH ĐỘNG ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Nút phát Ghi âm (Đổi sang màu Cam/Đỏ để nổi bật tính năng)
                if (!item.ghiAm.isNullOrEmpty()) {
                    Button(
                        onClick = { onPlayAudio("${AppConfig.HTTP_BASE_URL}${item.ghiAm}") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPlaying) Color.Black else Color(0xFFEA580C) // Cam đậm khi chưa chạy, Đen khi đang phát
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (isPlaying) "Dừng" else "Ghi âm",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 2. Nút Hủy tín hiệu cứu hộ
                if (item.trangThai == "CHO_XU_LY" || item.trangThai == "PENDING") {
                    OutlinedButton(
                        onClick = onCancelClick,
                        border = BorderStroke(1.dp, PrimaryRed),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryRed),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Hủy", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.weight(1f)) // Đẩy nút hóa đơn về phía bên phải cho gọn gàng

                // 3. Trạng thái hoặc Nút xem Hóa đơn
                item.trangThaiHoaDon?.let { status ->
                    when (status) {
                        "PENDING" -> {
                            Button(
                                onClick = onViewInvoiceDetail,
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed), // Đỏ đặc trưng cho hóa đơn chờ xử lý
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Xem hóa đơn", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        "SUCCESS" -> {
                            Button(
                                onClick = onViewInvoiceDetail,
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen), // Xanh lá cây tượng trưng cho hóa đơn đã hoàn thành
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Xem hóa đơn", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE5E7EB), shape = RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Hóa đơn: $status",
                                    color = TextDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                } ?: Text(
                    text = "Chưa có hóa đơn",
                    color = TextGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}
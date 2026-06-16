package com.example.canhbao.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
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
import com.example.canhbao.data.model.suco.baocao.TheoDoiSuCoItemResponseDTO // ✅ Đã đổi sang nhận Item DTO
import com.example.canhbao.data.network.AppConfig

// Bảng màu xanh nước biển chủ đạo đại diện cho luồng Báo Cáo Sự Cố
private val PrimaryBlue = Color(0xFF1976D2)
private val LightBlueBg = Color(0xFFE3F2FD)
private val BorderBlue = Color(0xFF90CAF9)
private val TextDark = Color(0xFF1F2937)
private val TextGray = Color(0xFF6B7280)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaoCaoItemUI(
    item: TheoDoiSuCoItemResponseDTO,
    onDetailClick: (Long) -> Unit
) {
    // Ép kiểu/Kiểm tra ID an toàn, phòng hờ Backend trả về null
    val itemId = item.id ?: return

    // Xử lý chuỗi định dạng ngày tháng gọn gàng từ chuỗi ISO (yyyy-MM-ddThh:mm:ss)
    val displayDate = try {
        item.thoiGianTao
            ?.substringBefore("T")
            ?.split("-")
            ?.reversed()
            ?.joinToString("/")
            ?: "Không rõ thời gian"
    } catch (_: Exception) {
        "Không rõ thời gian"
    }

    OutlinedCard(
        onClick = { onDetailClick(itemId) },   // ✅ Click vào Item sẽ mở màn hình chi tiết (nơi chứa nút Hủy báo cáo)
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp), // Đồng bộ padding với luồng SOS
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Ảnh thu nhỏ của vụ sự cố công cộng (Thumbnail)
            Box(
                modifier = Modifier
                    .size(65.dp) // Kích thước bằng chuẩn với item cứu hộ SOS
                    .clip(RoundedCornerShape(8.dp))
                    .background(LightBlueBg)
            ) {
                if (item.hinhAnhUrl.isNullOrEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(24.dp).align(Alignment.Center)
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

            Spacer(Modifier.width(12.dp))

            // 2. Khu vực thông tin cơ bản của sự cố
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Tiêu đề: Tên loại sự cố báo cáo
                Text(
                    text = item.tenLoai ?: "Sự cố không rõ loại",
                    color = TextDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Trạm/Trụ sở chịu trách nhiệm xử lý
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Home, null, tint = TextGray, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Trạm: ${item.tenTruSoTiepNhan ?: "Đang tìm kiếm..."}",
                        color = TextGray,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Thời gian người dùng gửi báo cáo lên hệ thống
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, tint = TextGray, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = displayDate,
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.width(4.dp))

            // 3. Mũi tên điều hướng ẩn dụ thúc đẩy hành động Click xem chi tiết
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier
                    .size(14.dp)
                    .padding(end = 2.dp)
            )
        }
    }
}
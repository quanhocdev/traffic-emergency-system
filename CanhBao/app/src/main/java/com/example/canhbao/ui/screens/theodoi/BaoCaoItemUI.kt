package com.example.canhbao.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.canhbao.data.model.suco.baocao.TheoDoiSuCoItemResponseDTO
import com.example.canhbao.data.network.AppConfig

private val PrimaryBlue = Color(0xFF1976D2)
private val LightBlueBg = Color(0xFFE3F2FD)
private val BorderBlue = Color(0xFF90CAF9)
private val TextDark = Color(0xFF1F2937)
private val TextGray = Color(0xFF6B7280)
private val CancelRed = Color(0xFFDC2626) // Màu đỏ nút hủy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaoCaoItemUI(
    item: TheoDoiSuCoItemResponseDTO,
    onDetailClick: (Long) -> Unit,
    onCancelClick: (Long) -> Unit // 1. Thêm callback để xử lý sự kiện hủy
) {
    val itemId = item.id ?: return

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
        onClick = { onDetailClick(itemId) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        // Chuyển sang bọc Column bên ngoài để nếu có nút Hủy sẽ xếp xuống hàng dưới
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Ảnh thu nhỏ (Thumbnail)
                Box(
                    modifier = Modifier
                        .size(65.dp)
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

                // 2. Khu vực thông tin cơ bản
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.tenLoai ?: "Sự cố không rõ loại",
                        color = TextDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

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

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, null, tint = TextGray, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(text = displayDate, color = TextGray, fontSize = 12.sp)
                    }
                }

                Spacer(Modifier.width(4.dp))

                // 3. Mũi tên đi tiếp
                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(14.dp)
                )
            }

             if (item.trangThaiXuLy == "Chờ xử lý" || item.trangThaiXuLy == "Đã tiếp nhận") {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { onCancelClick(itemId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp), // Thu nhỏ chiều cao lại một chút cho cân đối với Card
                    border = BorderStroke(1.dp, CancelRed),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CancelRed),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Hủy Báo Cáo", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
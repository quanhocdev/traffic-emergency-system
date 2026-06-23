package com.example.canhbao.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.canhbao.data.model.sos.tinhieu.TheoDoiSOSItemResponseDTO
import com.example.canhbao.data.network.AppConfig

private val PrimaryRed = Color(0xFFDC2626)
private val LightRedBg = Color(0xFFFEF2F2)
private val BorderRed = Color(0xFFFCA5A5)
private val TextDark = Color(0xFF1F2937)
private val TextGray = Color(0xFF6B7280)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TinHieuItemUI(
    item: TheoDoiSOSItemResponseDTO,
    onDetailClick: (Long) -> Unit,
    onCancelClick: (Long) -> Unit
) {
    val itemId = item.id ?: return

    OutlinedCard(
        onClick = { onDetailClick(itemId) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderRed),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Ảnh thu nhỏ sự cố
            Box(
                modifier = Modifier
                    .size(65.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LightRedBg)
            ) {
                if (item.hinhAnh.isNullOrEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = PrimaryRed,
                        modifier = Modifier.size(24.dp).align(Alignment.Center)
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

            Spacer(Modifier.width(12.dp))

            // 2. Thông tin cơ bản
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Mã cứu hộ: #${item.id}",
                    color = TextDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Home, null, tint = TextGray, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Trạm: ${item.tenTruSo ?: "Đang tìm kiếm..."}",
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, tint = TextGray, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = item.createdAt ?: "Không rõ thời gian",
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }
            }

            // 3. Nút mũi tên chuyển trang ẩn dụ thúc đẩy click hành động
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(16.dp).padding(end = 4.dp)
            )
        }
        // 4. Kiểm tra trạng thái để hiển thị nút Hủy SOS
        if (item.trangThai == "DA_TIEP_NHAN") {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { onCancelClick(itemId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                border = BorderStroke(1.dp, PrimaryRed),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryRed),
                shape = RoundedCornerShape(6.dp)
            ) {
                Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Hủy tín hiệu", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
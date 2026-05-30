package com.example.canhbao.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
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
import com.example.canhbao.data.model.suco.baocao.TheoDoiBaoCaoResponseDTO
import com.example.canhbao.data.network.AppConfig

@Composable
fun BaoCaoItemUI(
    item: TheoDoiBaoCaoResponseDTO,
    onCancelClick: () -> Unit
) {

    val activeColor = Color(0xFF1976D2)

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

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White
        )
    ) {

        Column(
            modifier = Modifier.padding(12.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {

                Box(
                    modifier = Modifier
                        .size(85.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE3F2FD))
                ) {

                    if (item.hinhAnhUrl.isNullOrEmpty()) {
                        Icon(
                            Icons.Default.Build,
                            null,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(32.dp),
                            tint = activeColor
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

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = item.tenLoai ?: "Sự cố",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = activeColor
                    )

                    item.moTa?.let {
                        Text(
                            text = it,
                            fontSize = 13.sp,
                            color = Color.DarkGray,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = activeColor
                        )

                        Text(
                            text = " ${item.diaChi ?: "Không rõ"}",
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )

                        Text(
                            text = " $displayDate",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            AssistChip(
                onClick = {},
                label = {
                    Text("Duyệt: ${item.trangThaiDuyet ?: "---"}")
                }
            )

            Spacer(Modifier.height(6.dp))

            AssistChip(
                onClick = {},
                label = {
                    Text("Xử lý: ${item.trangThaiXuLy ?: "---"}")
                }
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Độ tin cậy: ${item.doTinCay ?: 0}%",
                fontSize = 12.sp
            )

            Text(
                text = "Mức độ: ${item.mucDoNghiemTrong ?: "---"}",
                fontSize = 12.sp
            )

            Text(
                text = "Tiếp nhận: ${item.tenTruSoTiepNhan ?: "Chưa có"}",
                fontSize = 12.sp
            )

            Spacer(Modifier.height(12.dp))

            if (
                item.trangThaiXuLy == "CHO_XU_LY" &&
                item.trangThaiDuyet == "AI_APPROVED"
            ) {

                OutlinedButton(
                    onClick = onCancelClick,
                    border = BorderStroke(
                        1.dp,
                        Color.Red
                    )
                ) {
                    Text(
                        "Hủy báo cáo",
                        color = Color.Red
                    )
                }
            }
        }
    }
}
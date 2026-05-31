package com.example.canhbao.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.canhbao.data.model.hoadon.payment.ThanhToanResponseDTO
import com.example.canhbao.data.model.sos.tinhieu.TheoDoiTinHieuResponseDTO
import com.example.canhbao.data.network.AppConfig

@Composable
fun TinHieuItemUI(
    item: TheoDoiTinHieuResponseDTO,
    invoice: ThanhToanResponseDTO?,
    isPlaying: Boolean,
    onPlayAudio: (String) -> Unit,
    onCancelClick: () -> Unit,
    onPayClick: () -> Unit,
    onViewInvoiceDetail: () -> Unit
) {

    val activeColor = Color(0xFFD32F2F)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {

        Column(
            modifier = Modifier.padding(12.dp)
        ) {

            Row {

                Box(
                    modifier = Modifier
                        .size(85.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFEBEE))
                ) {

                    if (item.hinhAnh.isNullOrEmpty()) {

                        Icon(
                            Icons.Default.Warning,
                            null,
                            modifier = Modifier.align(Alignment.Center)
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

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        "Yêu cầu cứu hộ",
                        color = activeColor
                    )

                    Text(
                        item.ghiChu ?: "",
                        fontSize = 13.sp
                    )

                    Row {
                        Icon(
                            Icons.Default.LocationOn,
                            null,
                            modifier = Modifier.size(14.dp)
                        )

                        Text(
                            item.diaChi ?: "Không rõ vị trí",
                            fontSize = 11.sp
                        )
                    }

                    Text(
                        "Tiếp nhận: ${item.tenTruSoTiepNhan ?: "Chưa có"}",
                        fontSize = 12.sp
                    )

                    Text(
                        "Trạng thái: ${item.trangThai ?: "---"}",
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                if (!item.ghiAm.isNullOrEmpty()) {

                    Button(
                        onClick = {
                            onPlayAudio(
                                "${AppConfig.HTTP_BASE_URL}${item.ghiAm}"
                            )
                        }
                    ) {

                        Icon(
                            if (isPlaying)
                                Icons.Default.Stop
                            else
                                Icons.Default.PlayArrow,
                            null
                        )

                        Text(
                            if (isPlaying)
                                " Dừng"
                            else
                                " Ghi âm"
                        )
                    }
                }

                if (
                    item.trangThai == "CHO_XU_LY" ||
                    item.trangThai == "PENDING"
                ) {

                    OutlinedButton(
                        onClick = onCancelClick,
                        border = BorderStroke(
                            1.dp,
                            Color.Red
                        )
                    ) {
                        Text(
                            "Hủy",
                            color = Color.Red
                        )
                    }
                }

                when (invoice?.trangThai) {

                    "PENDING" -> {

                        Button(
                            onClick = onPayClick
                        ) {
                            Icon(
                                Icons.Default.Payment,
                                null
                            )
                            Text(" Thanh toán")
                        }
                    }

                    "SUCCESS" -> {

                        Button(
                            onClick = onViewInvoiceDetail
                        ) {
                            Text("Xem hóa đơn")
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            item.thanhTien?.let {

                Text(
                    text = "Chi phí: ${String.format("%,.0f", it)} đ"
                )
            }

            item.trangThaiHoaDon?.let {

                Text(
                    text = "Hóa đơn: $it"
                )
            }
        }
    }
}
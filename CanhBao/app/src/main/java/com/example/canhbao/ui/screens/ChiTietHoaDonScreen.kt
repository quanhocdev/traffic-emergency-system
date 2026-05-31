package com.example.canhbao.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.canhbao.viewmodel.ChiTietHoaDonViewModel

@androidx.compose.runtime.Composable
fun ChiTietHoaDonScreen(
    hoaDonId: Long,
    navController: NavController,
    viewModel: ChiTietHoaDonViewModel = viewModel()
) {

    val payment by androidx.compose.runtime.remember {
        androidx.compose.runtime.derivedStateOf {
            viewModel.paymentDetail
        }
    }

    LaunchedEffect(hoaDonId) {
        viewModel.loadPaymentDetail(hoaDonId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        TextButton(
            onClick = {
                navController.popBackStack()
            }
        ) {
            Text("← Quay lại")
        }

        Spacer(Modifier.height(16.dp))

        when {

            viewModel.isLoading -> {

                CircularProgressIndicator()
            }

            viewModel.errorMessage != null -> {

                Text(
                    text = viewModel.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }

            payment != null -> {

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        Text(
                            "Chi tiết thanh toán",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Text(
                            "Mã thanh toán: ${payment!!.thanhToanId}"
                        )

                        Text(
                            "Mã hóa đơn: ${payment!!.hoaDonId}"
                        )

                        Text(
                            "Phương thức: ${payment!!.phuongThucThanhToan}"
                        )

                        Text(
                            "Trạng thái: ${payment!!.trangThai}"
                        )

                        Text(
                            "Tiền gốc: ${payment!!.thanhTien}"
                        )

                        Text(
                            "Giảm giá: ${payment!!.soTienGiam}"
                        )

                        Text(
                            "Tổng thanh toán: ${payment!!.tongThanhToan}"
                        )

                        Text(
                            "Ngày thanh toán: ${payment!!.createdAt}"
                        )
                    }
                }
            }
        }
    }
}
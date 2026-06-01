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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
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

    val hoaDon by remember {
        derivedStateOf {
            viewModel.hoaDon
        }
    }
    LaunchedEffect(hoaDonId) {
        viewModel.loadHoaDonDetail(hoaDonId)
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

        Spacer(
            modifier = Modifier.height(16.dp)
        )

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

            hoaDon != null -> {

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        Text(
                            text = "Chi tiết hóa đơn",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Text(
                            text = "Mã hóa đơn: ${hoaDon!!.id}"
                        )

                        Text(
                            text = "Nội dung xử lý: ${hoaDon!!.noiDungXuLy}"
                        )

                        Text(
                            text = "Chi phí: ${hoaDon!!.thanhTien}"
                        )

                        Text(
                            text = "Trạng thái: ${hoaDon!!.trangThai}"
                        )

                        Text(
                            text = "Ngày tạo: ${hoaDon!!.createdAt}"
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        when (hoaDon!!.trangThai) {

                            "PENDING" -> {

                                Button(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        navController.navigate(
                                            "thanh_toan/${hoaDon!!.id}"
                                        )
                                    }
                                ) {
                                    Text("Thanh toán")
                                }
                            }

                            "SUCCESS" -> {

                                Button(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        navController.navigate(
                                            "chi_tiet_thanh_toan/${hoaDon!!.id}"
                                        )
                                    }
                                ) {
                                    Text("Xem chi tiết thanh toán")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
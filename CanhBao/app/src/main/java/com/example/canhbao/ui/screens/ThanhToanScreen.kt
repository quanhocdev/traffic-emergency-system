package com.example.canhbao.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.ui.res.painterResource

import com.example.canhbao.R
import com.example.canhbao.data.model.qua.doiqua.TuiQuaResponseDTO
import com.example.canhbao.viewmodel.ThanhToanViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThanhToanScreen(
    hoaDonId: Long,
    navController: NavController,
    viewModel: ThanhToanViewModel = viewModel()
) {

    var selectedVoucher by remember {
        mutableStateOf<TuiQuaResponseDTO?>(null)
    }

    var paymentMethod by remember {
        mutableStateOf("MOMO")
    }

    LaunchedEffect(Unit) {
        viewModel.loadVoucher()
    }

    LaunchedEffect(viewModel.paymentSuccess) {

        if (viewModel.paymentSuccess) {

            navController.navigate(
                "chi_tiet_hoa_don/$hoaDonId"
            ) {
                popUpTo(
                    "thanh_toan/$hoaDonId"
                ) {
                    inclusive = true
                }
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            "Thanh toán hóa đơn",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(16.dp))

        Text("Quét QR để thanh toán")

        Image(
            painter = painterResource(R.drawable.qr_goi),
            contentDescription = null,
            modifier = Modifier.size(220.dp)
        )

        Spacer(Modifier.height(20.dp))

        Text("Voucher")

        viewModel.listVoucher
            .filter {
                it.loai == "VOUCHER" &&
                        (it.soLuong ?: 0) > 0
            }
            .forEach { voucher ->

                val selected =
                    selectedVoucher?.quaId ==
                            voucher.quaId

                OutlinedCard(
                    onClick = {
                        selectedVoucher =
                            if (selected) null
                            else voucher
                    }
                ) {

                    Row(
                        modifier = Modifier.padding(12.dp)
                    ) {

                        Text(
                            voucher.tenQua
                        )

                        Spacer(
                            Modifier.weight(1f)
                        )

                        RadioButton(
                            selected = selected,
                            onClick = null
                        )
                    }
                }
            }

        Spacer(Modifier.height(16.dp))

        Text("Phương thức thanh toán")

        Row {

            RadioButton(
                selected =
                    paymentMethod == "MOMO",
                onClick = {
                    paymentMethod = "MOMO"
                }
            )

            Text("MOMO")
        }

        Row {

            RadioButton(
                selected =
                    paymentMethod == "BANKING",
                onClick = {
                    paymentMethod = "BANKING"
                }
            )

            Text("Chuyển khoản")
        }

        Spacer(
            Modifier.height(24.dp)
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {

                viewModel.thanhToan(
                    hoaDonId = hoaDonId,
                    quaId = selectedVoucher?.quaId,
                    phuongThuc = paymentMethod
                )
            }
        ) {

            Text(
                "Tôi đã thanh toán"
            )
        }
    }
}
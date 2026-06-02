package com.example.canhbao.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment

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

    LaunchedEffect(Unit) {
        snapshotFlow { viewModel.paymentSuccess }
            .collect { success ->
                if (success) {
                    viewModel.resetPayment() // thêm hàm này
                    navController.navigate("chi_tiet_hoa_don/$hoaDonId") {
                        popUpTo("thanh_toan/$hoaDonId") { inclusive = true }
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

        Text(
            "Voucher giảm giá",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(8.dp))

        val vouchers = viewModel.listVoucher.filter {
            it.loai == "VOUCHER" && (it.soLuong ?: 0) > 0
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // 1. render voucher nếu có
            items(vouchers) { voucher ->

                val selected = selectedVoucher?.quaId == voucher.quaId

                Card(
                    onClick = {
                        selectedVoucher = if (selected) null else voucher
                    },
                    modifier = Modifier.size(width = 160.dp, height = 90.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(voucher.tenQua)
                        RadioButton(selected = selected, onClick = null)
                    }
                }
            }

            // 2. NÚT + LUÔN LUÔN HIỆN (QUAN TRỌNG)
            item {
                Card(
                    modifier = Modifier.size(width = 90.dp, height = 90.dp),
                    onClick = {
                        navController.navigate("qua_screen")
                    }
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text("Đổi quà")
                    }
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
        if (viewModel.loading) {
            CircularProgressIndicator()
        }

        if (viewModel.errorMessage != null) {
            Text(
                text = viewModel.errorMessage!!,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (viewModel.paymentSuccess) {
            Text("Thanh toán thành công ✅")
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.loading,
            onClick = {
                viewModel.thanhToan(
                    hoaDonId = hoaDonId,
                    quaId = selectedVoucher?.quaId,
                    phuongThuc = paymentMethod
                )
            }
        ) {
            if (viewModel.loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Thanh toán")
            }
        }
    }
}
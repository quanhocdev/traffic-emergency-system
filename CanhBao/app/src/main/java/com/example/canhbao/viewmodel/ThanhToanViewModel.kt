package com.example.canhbao.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canhbao.data.model.hoadon.payment.ThanhToanRequestDTO
import com.example.canhbao.data.model.qua.doiqua.TuiQuaResponseDTO
import com.example.canhbao.data.network.BaoCaoSuCoRetrofit.api
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ThanhToanViewModel : ViewModel() {

    var listVoucher by mutableStateOf<List<TuiQuaResponseDTO>>(emptyList())
        private set

    var loading by mutableStateOf(false)
        private set

    var paymentSuccess by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private suspend fun getToken(): String {

        val user =
            FirebaseAuth.getInstance()
                .currentUser
                ?: throw Exception("Chưa đăng nhập")

        val tokenResult =
            Tasks.await(
                user.getIdToken(false)
            )

        return "Bearer ${tokenResult.token}"
    }

    fun loadVoucher() {
        viewModelScope.launch {
            try {
                loading = true

                val result = withContext(Dispatchers.IO) {

                    val token = getToken()
                    Log.d("VOUCHER", "TOKEN = $token")   // 👈 LOG 1

                    val res = api.getMyGifts(token)

                    Log.d("VOUCHER", "RAW RESULT = $res") // 👈 LOG 2

                    res
                }

                listVoucher = result

                Log.d("VOUCHER", "LIST SIZE = ${listVoucher.size}") // 👈 LOG 3

            } catch (e: Exception) {

                errorMessage = e.message

                Log.e("VOUCHER", "ERROR LOAD VOUCHER", e) // 👈 LOG 4

            } finally {
                loading = false
            }
        }
    }
    fun thanhToan(
        hoaDonId: Long,
        quaId: Long?,
        phuongThuc: String
    ) {
        viewModelScope.launch {
            try {
                loading = true
                paymentSuccess = false // Luôn reset về false khi bắt đầu bấm nút
                errorMessage = null

                val response = withContext(Dispatchers.IO) {
                    val token = getToken()

                    // Thay đổi: Gọi lấy nguyên đối tượng Response thay vì chỉ lấy .isSuccessful
                    api.confirmPayment(
                        token,
                        ThanhToanRequestDTO(
                            hoaDonId = hoaDonId,
                            quaId = quaId,
                            phuongThucThanhToan = phuongThuc
                        )
                    )
                }

                if (response.isSuccessful) {
                    // CỰC KỲ QUAN TRỌNG: Chỉ bật true khi thực sự thành công
                    paymentSuccess = true
                } else {
                    // THẤT BẠI: Giữ nguyên success là false và đọc nội dung lỗi từ Spring Boot
                    paymentSuccess = false
                    val errorBodyString = response.errorBody()?.string()
                    errorMessage = if (!errorBodyString.isNullOrBlank()) {
                        errorBodyString
                    } else {
                        "Thanh toán thất bại (Mã lỗi: ${response.code()})"
                    }
                    Log.e("ThanhToanVM", "Backend trả về lỗi công việc: $errorMessage")
                }

            } catch (e: Exception) {
                paymentSuccess = false
                errorMessage = "Lỗi kết nối: ${e.localizedMessage}"
                Log.e("ThanhToanVM", "Lỗi ngoại lệ hệ thống", e)
            } finally {
                loading = false
            }
        }
    }
    fun resetPayment() {
        paymentSuccess = false
    }
}
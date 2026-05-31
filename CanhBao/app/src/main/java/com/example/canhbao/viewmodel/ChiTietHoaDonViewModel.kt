package com.example.canhbao.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canhbao.data.model.hoadon.payment.ThanhToanResponseDTO
import com.example.canhbao.data.network.BaoCaoSuCoRetrofit.api
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChiTietHoaDonViewModel : ViewModel() {

    var paymentDetail by mutableStateOf<ThanhToanResponseDTO?>(null)
        private set

    var isLoading by mutableStateOf(false)
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

    fun loadPaymentDetail(
        hoaDonId: Long
    ) {

        viewModelScope.launch {

            try {

                isLoading = true
                errorMessage = null

                val result =
                    withContext(Dispatchers.IO) {

                        val token = getToken()

                        api.getChiTietThanhToan(
                            token,
                            hoaDonId
                        )
                    }

                paymentDetail = result

            } catch (e: Exception) {

                errorMessage =
                    e.message ?: "Lỗi tải dữ liệu"

                Log.e(
                    "ChiTietHoaDon",
                    e.message ?: ""
                )

            } finally {

                isLoading = false
            }
        }
    }
}
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

                val result =
                    withContext(Dispatchers.IO) {

                        val token = getToken()

                        api.getMyGifts(token)
                    }

                listVoucher = result

            } catch (e: Exception) {

                errorMessage = e.message

                Log.e(
                    "ThanhToanVM",
                    e.message ?: ""
                )

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

                val success =
                    withContext(Dispatchers.IO) {

                        val token = getToken()

                        api.confirmPayment(
                            token,
                            ThanhToanRequestDTO(
                                hoaDonId = hoaDonId,
                                quaId = quaId,
                                phuongThucThanhToan = phuongThuc
                            )
                        ).isSuccessful
                    }

                paymentSuccess = success

            } catch (e: Exception) {

                errorMessage = e.message

                Log.e(
                    "ThanhToanVM",
                    e.message ?: ""
                )

            } finally {

                loading = false
            }
        }
    }
}
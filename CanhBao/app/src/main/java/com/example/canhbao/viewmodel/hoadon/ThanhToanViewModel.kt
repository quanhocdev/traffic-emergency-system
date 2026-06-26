package com.example.canhbao.viewmodel.hoadon

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canhbao.data.model.hoadon.HoaDonUserResponseDTO
import com.example.canhbao.data.model.hoadon.payment.ThanhToanRequestDTO
import com.example.canhbao.data.model.hoadon.payment.ThanhToanResponseDTO
import com.example.canhbao.data.model.qua.doiqua.TuiQuaResponseDTO
import com.example.canhbao.data.network.BaoCaoSuCoRetrofit
import com.example.canhbao.data.network.SocketClientProvider
import com.example.canhbao.data.network.UserSocketManager
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
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

    var hoaDonDetail by mutableStateOf<HoaDonUserResponseDTO?>(null)
        private set

    var paymentInfo by mutableStateOf<ThanhToanResponseDTO?>(null)
        private set

    // Quản lý instance UserSocketManager dựa trên file gốc của bạn
    private var userSocketManager: UserSocketManager? = null
    private val gson = Gson()

    private suspend fun getToken(): String {
        val user = FirebaseAuth.getInstance().currentUser ?: throw Exception("Chưa đăng nhập")
        val tokenResult = Tasks.await(user.getIdToken(false))
        return "Bearer ${tokenResult.token}"
    }

    @SuppressLint("CheckResult")
    private fun connectWebSocket() {
        val currentClient = SocketClientProvider.stompClient
        if (userSocketManager != null && currentClient.isConnected) return

        userSocketManager = null
        SocketClientProvider.initNewClient()
        val activeClient = SocketClientProvider.stompClient

        // Đăng ký nhận cập nhật trạng thái hóa đơn thanh toán
        userSocketManager = UserSocketManager(activeClient).apply {
            subscribe(object : UserSocketManager.Callback {
                override fun onInvoiceUpdate(json: String) {
                    try {
                        val response = gson.fromJson(json, ThanhToanResponseDTO::class.java)
                        paymentInfo = response
                        loadVoucher()
                        Log.d("ThanhToanSocket", "🟢 Nhận thông tin cập nhật hóa đơn thành công")
                    } catch (e: Exception) {
                        Log.e("ThanhToanSocket", "Lỗi parse Invoice JSON: ${e.message}")
                    }
                }

                override fun onNewInvoice(json: String) {}
                override fun onHistoryRefresh() {}
                override fun onPackageRefresh() {}
                override fun onSosRefresh() {}
                override fun onUserStats(json: String) {}
                override fun onPaymentUpdate(json: String) {}
            })
        }

        activeClient.connect()
    }

    fun startSocket() {
        connectWebSocket()
    }

    fun loadVoucher() {
        viewModelScope.launch {
            try {
                loading = true
                val result = withContext(Dispatchers.IO) {
                    val token = getToken()
                    Log.d("VOUCHER", "TOKEN = $token")
                    val res = BaoCaoSuCoRetrofit.api.getMyGifts(token)
                    Log.d("VOUCHER", "RAW RESULT = $res")
                    res
                }
                listVoucher = result
                Log.d("VOUCHER", "LIST SIZE = ${listVoucher.size}")
            } catch (e: Exception) {
                errorMessage = e.message
                Log.e("VOUCHER", "ERROR LOAD VOUCHER", e)
            } finally {
                loading = false
            }
        }
    }

    fun thanhToan(hoaDonId: Long, quaId: Long?, phuongThuc: String) {
        viewModelScope.launch {
            try {
                loading = true
                paymentSuccess = false
                errorMessage = null

                val response = withContext(Dispatchers.IO) {
                    val token = getToken()
                    BaoCaoSuCoRetrofit.api.confirmPayment(
                        token,
                        ThanhToanRequestDTO(
                            hoaDonId = hoaDonId,
                            quaId = quaId,
                            phuongThucThanhToan = phuongThuc
                        )
                    )
                }

                if (response.isSuccessful) {
                    paymentSuccess = true
                } else {
                    paymentSuccess = false
                    val errorBodyString = response.errorBody()?.string()
                    errorMessage = if (!errorBodyString.isNullOrBlank()) {
                        errorBodyString
                    } else {
                        "Thanh toán thất bại (Mã lỗi: ${response.code()})"
                    }
                    Log.e("ThanhToanVM", "Backend trả về lỗi: $errorMessage")
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

    fun loadHoaDonDetail(hoaDonId: Long) {
        viewModelScope.launch {
            try {
                errorMessage = null
                val token = withContext(Dispatchers.IO) { getToken() }
                val result = withContext(Dispatchers.IO) {
                    BaoCaoSuCoRetrofit.api.getHoaDonDetail(token, hoaDonId)
                }
                hoaDonDetail = result
            } catch (e: Exception) {
                Log.e("ThanhToanVM", "LỖI TẢI CHI TIẾT HÓA ĐƠN", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        userSocketManager = null
        Log.w("ThanhToanVM", "🧹 Đã giải phóng UserSocketManager trong ThanhToanViewModel")
    }
}
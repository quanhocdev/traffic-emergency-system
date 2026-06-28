package com.example.canhbao.viewmodel.hoadon

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canhbao.data.auth.FirebaseTokenProvider
import com.example.canhbao.data.model.hoadon.HoaDonUserResponseDTO
import com.example.canhbao.data.network.BaoCaoSuCoRetrofit
import com.example.canhbao.data.network.SocketClientProvider
import com.example.canhbao.data.network.UserSocketManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChiTietHoaDonViewModel : ViewModel() {

    var hoaDon by mutableStateOf<HoaDonUserResponseDTO?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Quản lý instance UserSocketManager dựa trên file gốc của bạn
    private var userSocketManager: UserSocketManager? = null

    private var currentHoaDonId: Long? = null



    @SuppressLint("CheckResult")
    private fun connectWebSocket() {

        viewModelScope.launch {

            if (userSocketManager != null) return@launch

            val activeClient = SocketClientProvider.ensureConnected()

            userSocketManager = UserSocketManager(activeClient).apply {

                subscribe(object : UserSocketManager.Callback {

                    override fun onNewInvoice(json: String) {

                        Log.d(
                            "HoaDonSocket",
                            "Nhận thông báo hóa đơn mới -> Refresh"
                        )

                        currentHoaDonId?.let {
                            loadHoaDonDetail(it)
                        }
                    }

                    override fun onInvoiceUpdate(json: String) {}
                    override fun onHistoryRefresh() {}
                    override fun onPackageRefresh() {}
                    override fun onSosRefresh() {}
                    override fun onUserStats(json: String) {}
                    override fun onPaymentUpdate(json: String) {}
                })
            }
        }
    }

    fun loadHoaDonDetail(hoaDonId: Long) {

        currentHoaDonId = hoaDonId

        connectWebSocket()

        viewModelScope.launch {

            try {

                Log.d("ChiTietHoaDon", "STEP 1")

                isLoading = true
                errorMessage = null

                val token = FirebaseTokenProvider.getToken()

                hoaDon = BaoCaoSuCoRetrofit.api.getHoaDonDetail(
                    token,
                    hoaDonId
                )

            } catch (e: Exception) {

                Log.e("ChiTietHoaDon", "MAIN ERROR", e)

                errorMessage = e.message ?: "Lỗi tải dữ liệu"

            } finally {

                isLoading = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        userSocketManager = null
        Log.w("ChiTietHoaDon", "🧹 Đã giải phóng UserSocketManager trong ChiTietHoaDonViewModel")
    }
}
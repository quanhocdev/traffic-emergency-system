package com.example.canhbao.viewmodel.hoadon

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canhbao.data.model.hoadon.HoaDonUserResponseDTO
import com.example.canhbao.data.network.BaoCaoSuCoRetrofit
import com.example.canhbao.data.network.SocketClientProvider
import com.example.canhbao.data.network.UserSocketManager
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
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

            val token = withContext(Dispatchers.IO) {
                try {
                    getToken().removePrefix("Bearer ")
                } catch (e: Exception) {
                    null
                }
            } ?: return@launch

            val currentClient = runCatching {
                SocketClientProvider.stompClient
            }.getOrNull()

            if (userSocketManager != null &&
                currentClient?.isConnected == true
            ) {
                return@launch
            }

            userSocketManager = null

            SocketClientProvider.initNewClient(token)

            val activeClient = SocketClientProvider.stompClient

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

        val currentClient = SocketClientProvider.stompClient
        if (!currentClient.isConnected) {
            connectWebSocket()
        }

        viewModelScope.launch {
            try {
                Log.d("ChiTietHoaDon", "STEP 1 - Start loadHoaDonDetail")
                isLoading = true
                errorMessage = null

                val token = withContext(Dispatchers.IO) {
                    Log.d("ChiTietHoaDon", "STEP 2 - Getting token")
                    getToken()
                }

                Log.d("ChiTietHoaDon", "STEP 3 - Got token")

                hoaDon = withContext(Dispatchers.IO) {
                    Log.d("ChiTietHoaDon", "STEP 4 - Calling getHoaDonDetail")
                    BaoCaoSuCoRetrofit.api.getHoaDonDetail(token, hoaDonId)
                }

                Log.d("ChiTietHoaDon", "STEP 5 - Loaded hoaDon")
                Log.d("ChiTietHoaDon", "STEP 7 - Finished")

            } catch (e: Exception) {
                Log.e("ChiTietHoaDon", "MAIN ERROR", e)
                errorMessage = e.message ?: "Lỗi tải dữ liệu"
            } finally {
                isLoading = false
                Log.d("ChiTietHoaDon", "STEP 8 - End")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        userSocketManager = null
        Log.w("ChiTietHoaDon", "🧹 Đã giải phóng UserSocketManager trong ChiTietHoaDonViewModel")
    }
}
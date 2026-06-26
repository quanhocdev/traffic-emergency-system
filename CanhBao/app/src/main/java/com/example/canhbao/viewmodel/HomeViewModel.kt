package com.example.canhbao.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canhbao.data.model.suco.baocao.SuCoUserDto
import com.example.canhbao.data.network.BaoCaoSuCoRetrofit
import com.example.canhbao.data.network.SocketClientProvider
import com.example.canhbao.data.network.UserSocketManager
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel : ViewModel() {

    var userDetail by mutableStateOf<SuCoUserDto?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    // Quản lý instance UserSocketManager dựa trên file bạn cung cấp
    private var userSocketManager: UserSocketManager? = null
    private val gson = Gson()

    // Lấy thông tin ban đầu và kết nối Socket
    fun initUserData() {
        fetchUserInfo()
        connectUserSocket()
    }

    private fun fetchUserInfo() {
        viewModelScope.launch {
            isLoading = true
            try {
                val user = FirebaseAuth.getInstance().currentUser ?: return@launch
                val token = user.getIdToken(false).await().token ?: return@launch

                val response = BaoCaoSuCoRetrofit.api.getUserInfo("Bearer $token")
                userDetail = response

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    private fun connectUserSocket() {
        val currentClient = SocketClientProvider.stompClient
        // Nếu đã khởi tạo manager và client cũ đang thông suốt thì không tạo đè
        if (userSocketManager != null && currentClient.isConnected) return

        userSocketManager = null
        SocketClientProvider.initNewClient()
        val activeClient = SocketClientProvider.stompClient

        // Khởi tạo và subscribe qua UserSocketManager gốc của bạn
        userSocketManager = UserSocketManager(activeClient).apply {
            subscribe(object : UserSocketManager.Callback {

                override fun onUserStats(json: String) {
                    try {
                        val updatedUser = gson.fromJson(json, SuCoUserDto::class.java)
                        // Đồng bộ trực tiếp lên UI State Compose
                        userDetail = updatedUser
                        Log.d("WebSocket_Home", "✅ Đã cập nhật User Stats thành công")
                    } catch (e: Exception) {
                        Log.e("WebSocket_Home", "Lỗi parse UserStats JSON: ${e.message}")
                    }
                }

                override fun onHistoryRefresh() {
                    Log.d("WebSocket_Home", "Yêu cầu refresh lịch sử")
                }

                override fun onPackageRefresh() {
                    Log.d("WebSocket_Home", "Yêu cầu refresh gói cứu trợ")
                }

                override fun onSosRefresh() {
                    Log.d("WebSocket_Home", "Yêu cầu refresh trạng thái SOS")
                }

                override fun onInvoiceUpdate(json: String) {
                    Log.d("WebSocket_Home", "Cập nhật hóa đơn: $json")
                }

                override fun onNewInvoice(json: String) {
                    Log.d("WebSocket_Home", "Có hóa đơn mới: $json")
                }

                override fun onPaymentUpdate(json: String) {
                    Log.d("WebSocket_Home", "Cập nhật thanh toán: $json")
                }
            })
        }

        // Kích hoạt kết nối tập trung thông qua provider
        activeClient.connect()
        Log.d("WebSocket_Home", "🟢 Đã kích hoạt luồng kết nối User Socket thành công")
    }

    override fun onCleared() {
        super.onCleared()
        // Giải phóng Manager để tránh leak luồng
        userSocketManager = null
        Log.w("WebSocket_Home", "🧹 HomeViewModel OnCleared! Đã gỡ bỏ tham chiếu Socket Manager.")
    }
}
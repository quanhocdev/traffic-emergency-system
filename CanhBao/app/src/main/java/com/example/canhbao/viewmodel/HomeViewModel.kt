package com.example.canhbao.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canhbao.data.auth.FirebaseTokenProvider
import com.example.canhbao.data.model.suco.baocao.SuCoUserDto
import com.example.canhbao.data.network.BaoCaoSuCoRetrofit
import com.example.canhbao.data.network.SocketClientProvider
import com.example.canhbao.data.network.UserSocketManager
import com.google.gson.Gson
import kotlinx.coroutines.launch

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
                val token = FirebaseTokenProvider.getToken()

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

        viewModelScope.launch {

            try {

                val activeClient = SocketClientProvider.ensureConnected()

                if (userSocketManager != null) {
                    return@launch
                }

                userSocketManager =
                    UserSocketManager(activeClient).apply {

                        subscribe(object : UserSocketManager.Callback {

                            override fun onUserStats(json: String) {
                                try {

                                    userDetail =
                                        gson.fromJson(
                                            json,
                                            SuCoUserDto::class.java
                                        )

                                    Log.d(
                                        "WebSocket_Home",
                                        "Update User Stats"
                                    )

                                } catch (e: Exception) {

                                    Log.e(
                                        "WebSocket_Home",
                                        e.message ?: ""
                                    )
                                }
                            }

                            override fun onHistoryRefresh() {}
                            override fun onPackageRefresh() {}
                            override fun onSosRefresh() {}
                            override fun onInvoiceUpdate(json: String) {}
                            override fun onNewInvoice(json: String) {}
                            override fun onPaymentUpdate(json: String) {}
                        })
                    }

            } catch (e: Exception) {

                Log.e(
                    "WebSocket_Home",
                    "Socket lỗi ${e.message}"
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Giải phóng Manager để tránh leak luồng
        userSocketManager = null
        Log.w("WebSocket_Home", "🧹 HomeViewModel OnCleared! Đã gỡ bỏ tham chiếu Socket Manager.")
    }
}
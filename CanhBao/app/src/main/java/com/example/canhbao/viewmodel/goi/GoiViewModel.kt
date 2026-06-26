package com.example.canhbao.viewmodel.goi

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canhbao.data.model.sos.goi.GoiResponseDto
import com.example.canhbao.data.model.sos.goi.MuaGoiRequestDTO
import com.example.canhbao.data.model.sos.goi.MuaGoiUserResponseDto
import com.example.canhbao.data.network.BaoCaoSuCoApi
import com.example.canhbao.data.network.SocketClientProvider
import com.example.canhbao.data.network.UserSocketManager
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GoiViewModel(private val api: BaoCaoSuCoApi) : ViewModel() {

    private val _goiList = MutableStateFlow<List<GoiResponseDto>>(emptyList())
    val goiList: StateFlow<List<GoiResponseDto>> = _goiList

    private val _myPackages = MutableStateFlow<List<MuaGoiUserResponseDto>>(emptyList())
    val myPackages: StateFlow<List<MuaGoiUserResponseDto>> = _myPackages

    // Quản lý thông qua UserSocketManager tập trung
    private var userSocketManager: UserSocketManager? = null

    private val _socketErrorMessage = MutableStateFlow<String?>(null)
    val socketErrorMessage: StateFlow<String?> = _socketErrorMessage

    // Hàm lấy token đồng bộ an toàn phục vụ API cứu trợ
    private suspend fun getToken(): String? {
        return try {
            val user = FirebaseAuth.getInstance().currentUser ?: return null
            val tokenResult = Tasks.await(user.getIdToken(false))
            tokenResult.token
        } catch (e: Exception) {
            null
        }
    }

    // Kết nối Socket thông qua Provider tập trung
    fun connectSocket() {
        val currentClient = SocketClientProvider.stompClient
        if (userSocketManager != null && currentClient.isConnected) return

        userSocketManager = null
        SocketClientProvider.initNewClient()
        val activeClient = SocketClientProvider.stompClient

        userSocketManager = UserSocketManager(activeClient).apply {
            subscribe(object : UserSocketManager.Callback {

                override fun onPackageRefresh() {
                    Log.d("WebSocket_Goi", "🔄 Nhận tín hiệu làm mới Gói cứu trợ (REFRESH)")
                    fetchMyPackages()
                }

                override fun onHistoryRefresh() {}
                override fun onSosRefresh() {}
                override fun onInvoiceUpdate(json: String) {}
                override fun onUserStats(json: String) {}
                override fun onNewInvoice(json: String) {}
                override fun onPaymentUpdate(json: String) {}
            })
        }

        activeClient.connect()
    }

    fun fetchGoi() {
        viewModelScope.launch {
            try {
                val response = api.getDanhSachGoi()
                _goiList.value = response
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchMyPackages() {
        viewModelScope.launch {
            try {
                val token = withContext(Dispatchers.IO) { getToken() } ?: return@launch
                val response = api.getGoiCuaToi("Bearer $token")
                _myPackages.value = response
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearSocketError() {
        _socketErrorMessage.value = null
    }

    fun muaGoi(goiId: Long, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val token = withContext(Dispatchers.IO) { getToken() } ?: ""
                val response = api.dangKyMuaGoi(
                    "Bearer $token",
                    MuaGoiRequestDTO(goiId)
                )

                if (response.isSuccessful) {
                    fetchMyPackages()
                    onResult(true, null)
                } else {
                    onResult(false, response.errorBody()?.string())
                }
            } catch (e: Exception) {
                onResult(false, "Lỗi mạng")
            }
        }
    }

    fun huyGoi(idMuaGoi: Long, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val token = withContext(Dispatchers.IO) { getToken() } ?: ""
                val response = api.cancelMuaGoi("Bearer $token", idMuaGoi)

                if (response.isSuccessful) {
                    fetchMyPackages()
                    onResult(true, "Hủy thành công")
                } else {
                    onResult(false, response.errorBody()?.string())
                }
            } catch (e: Exception) {
                onResult(false, "Lỗi mạng")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        userSocketManager = null
        Log.w("WebSocket_Goi", "🧹 Đã giải phóng luồng tham chiếu Socket Manager tại GoiViewModel.")
    }
}
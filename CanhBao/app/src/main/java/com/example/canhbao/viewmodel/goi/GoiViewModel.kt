package com.example.canhbao.viewmodel.goi

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canhbao.data.model.sos.goi.GoiResponseDto
import com.example.canhbao.data.model.sos.goi.MuaGoiUserResponseDto
import com.example.canhbao.data.model.sos.goi.MuaGoiRequestDTO
import com.example.canhbao.data.network.AppConfig
import com.example.canhbao.data.network.BaoCaoSuCoApi
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.StompHeader

class GoiViewModel(private val api: BaoCaoSuCoApi) : ViewModel() {
    private val _goiList = MutableStateFlow<List<GoiResponseDto>>(emptyList())
    val goiList: StateFlow<List<GoiResponseDto>> = _goiList

    private val _myPackages = MutableStateFlow<List<MuaGoiUserResponseDto>>(emptyList())
    val myPackages: StateFlow<List<MuaGoiUserResponseDto>> = _myPackages

    private var stompClient: StompClient? = null

    private val _socketErrorMessage = MutableStateFlow<String?>(null)
    val socketErrorMessage: StateFlow<String?> = _socketErrorMessage

    // Hàm kết nối Socket bảo mật mới
    fun connectSocket() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        if (stompClient?.isConnected == true) return

        // Lấy Firebase Token không đồng bộ để đính kèm vào Header kết nối
        user.getIdToken(false).addOnSuccessListener { result ->
            val token = result.token ?: return@addOnSuccessListener
            val url = "${AppConfig.WS_BASE_URL}/ws-suco/websocket"

            stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, url)

            // 1. THÊM HEADER XÁC THỰC: Đưa Token vào lệnh CONNECT
            val headers = listOf(StompHeader("Authorization", "Bearer $token"))
            stompClient?.connect(headers)

            // 2. Nghe kênh bảo mật chung, bỏ hoàn toàn phần cộng chuỗi $uid
            stompClient?.topic("/user/queue/package-status")?.subscribe({ it ->
                val payload = it.payload

                viewModelScope.launch {
                    if (payload == "REFRESH") {
                        fetchMyPackages()
                    } else {
                        _socketErrorMessage.value = payload
                    }
                }
            }, { it.printStackTrace() })

        }.addOnFailureListener {
            Log.e("SocketAuth", "Lấy Firebase Token thất bại: ${it.message}")
        }
    }

    override fun onCleared() {
        try {
            if (stompClient != null && stompClient!!.isConnected) {
                stompClient?.disconnect()
            }
        } catch (e: Exception) {
            Log.e("Socket", "Error during disconnect: ${e.message}")
        }
        super.onCleared()
    }

    fun fetchGoi() {
        viewModelScope.launch {
            try {
                val response = api.getDanhSachGoi()
                _goiList.value = response
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun fetchMyPackages() {
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser ?: return@launch
                val token = user.getIdToken(false).result?.token ?: return@launch

                val response = api.getGoiCuaToi("Bearer $token")
                _myPackages.value = response
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearSocketError() { _socketErrorMessage.value = null }

    fun muaGoi(goiId: Long, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser ?: return@launch
                val token = user.getIdToken(false).result?.token ?: ""

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
                val user = FirebaseAuth.getInstance().currentUser ?: return@launch
                val token = user.getIdToken(false).result?.token ?: ""

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
}
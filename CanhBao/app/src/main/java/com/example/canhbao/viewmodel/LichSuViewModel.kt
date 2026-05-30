package com.example.canhbao.viewmodel

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canhbao.data.model.hoadon.payment.ThanhToanResponseDTO
import com.example.canhbao.data.model.LichSuDto
import com.example.canhbao.data.model.hoadon.payment.ThanhToanRequestDTO
import com.example.canhbao.data.model.qua.doiqua.TuiQuaResponseDTO
import com.example.canhbao.data.network.AppConfig
import com.example.canhbao.data.network.BaoCaoSuCoRetrofit.api
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import com.google.gson.Gson

sealed class LichSuUiState {
    object Loading : LichSuUiState()
    data class Success(val data: List<LichSuDto>) : LichSuUiState()
    data class Error(val message: String) : LichSuUiState()
}

class LichSuViewModel : ViewModel() {

    var uiState: LichSuUiState by mutableStateOf(LichSuUiState.Loading)
        private set

    var listTuiQua by mutableStateOf<List<TuiQuaResponseDTO>>(emptyList())
        private set

    var pendingInvoicesMap by mutableStateOf<Map<Long, ThanhToanResponseDTO>>(emptyMap())
        private set

    private var mStompClient: StompClient? = null
    private var mediaPlayer: MediaPlayer? = null
    var currentlyPlayingId by mutableStateOf<Long?>(null)

    // ================= TOKEN =================
    private suspend fun getToken(): String {
        val user = FirebaseAuth.getInstance().currentUser
            ?: throw Exception("Chưa đăng nhập")

        val tokenResult = Tasks.await(user.getIdToken(false))
        return "Bearer ${tokenResult.token ?: ""}"
    }

    // ================= FETCH =================
    fun fetchHistory() {
        loadDataFromApi()
        loadTuiQua()
        connectWebSocket()
    }

    private fun loadDataFromApi() {
        viewModelScope.launch {
            try {
                uiState = LichSuUiState.Loading

                val response = withContext(Dispatchers.IO) {
                    val token = getToken()
                    api.getAllHistory(token)
                }

                uiState = LichSuUiState.Success(response)

            } catch (e: Exception) {
                uiState = LichSuUiState.Error(
                    e.localizedMessage ?: "Lỗi kết nối Server"
                )
                Log.e("LichSuViewModel", "loadData error: ${e.message}")
            }
        }
    }

    private fun loadTuiQua() {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    val token = getToken()
                    api.getMyGifts(token)
                }
                listTuiQua = response
            } catch (e: Exception) {
                Log.e("LichSuViewModel", "loadTuiQua error: ${e.message}")
            }
        }
    }

    // ================= ACTIONS =================
    fun confirmPayment(hoaDonId: Long, quaId: Long?) {
        viewModelScope.launch {
            try {
                val isSuccessful = withContext(Dispatchers.IO) {
                    val token = getToken()
                    val request = ThanhToanRequestDTO(
                        hoaDonId = hoaDonId,
                        quaId = quaId,
                        phuongThucThanhToan = "MOMO"
                    )

                    val response = api.confirmPayment(token, request)
                    response.isSuccessful
                }

                if (isSuccessful) {
                    fetchHistory()
                }

            } catch (e: Exception) {
                Log.e("LichSuViewModel", "confirmPayment error: ${e.message}")
            }
        }
    }

    fun cancelOrder(item: LichSuDto) {
        viewModelScope.launch {
            try {
                val isSuccessful = withContext(Dispatchers.IO) {
                    val token = getToken()

                    val response = if (item.loai == "SOS") {
                        api.cancelSOS(token, item.id)
                    } else {
                        api.cancelSuCo(token, item.id)
                    }

                    response.isSuccessful
                }

                if (isSuccessful) {
                    fetchHistory()
                }

            } catch (e: Exception) {
                Log.e("LichSuViewModel", "cancelOrder error: ${e.message}")
            }
        }
    }

    // ================= WEBSOCKET =================
    @SuppressLint("CheckResult")
    private fun connectWebSocket() {
        if (mStompClient?.isConnected == true) return

        mStompClient = Stomp.over(
            Stomp.ConnectionProvider.OKHTTP,
            "${AppConfig.WS_BASE_URL}/ws-suco/websocket"
        )

        val topics = listOf(
            "/topic/user/history",
            "/topic/user/sos-status",
            "/topic/su-co"
        )

        topics.forEach { topic ->
            mStompClient?.topic(topic)?.subscribe({
                loadDataFromApi()
            }, {
                Log.e("WebSocket", "Error: ${it.message}")
            })
        }

        mStompClient?.topic("/topic/user/invoice")?.subscribe({ topicMessage ->

            val thanhToan = Gson().fromJson(
                topicMessage.payload,
                ThanhToanResponseDTO::class.java
            )

            viewModelScope.launch {
                val hoaDonId = thanhToan.hoaDonId ?: return@launch

                val currentMap = pendingInvoicesMap.toMutableMap()

                if (thanhToan.trangThai == "SUCCESS") {
                    currentMap.remove(hoaDonId)
                } else {
                    currentMap[hoaDonId] = thanhToan
                }

                pendingInvoicesMap = currentMap

                loadDataFromApi()
                loadTuiQua()
            }

        }, {
            Log.e("WebSocket", "Invoice error: ${it.message}")
        })
    }
        // ================= UTILS =================
        fun clearInvoice(hoaDonId: Long) {
            val currentMap = pendingInvoicesMap.toMutableMap()
            currentMap.remove(hoaDonId)
            pendingInvoicesMap = currentMap
        }

        // ================= AUDIO =================
        fun playRecording(url: String, id: Long) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    if (currentlyPlayingId == id) {
                        stopPlayback()
                        return@launch
                    }

                    stopPlayback()

                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(url)
                        prepare()
                        start()
                        currentlyPlayingId = id
                        setOnCompletionListener { stopPlayback() }
                    }

                } catch (e: Exception) {
                    currentlyPlayingId = null
                    Log.e("MediaPlayer", "Play error: ${e.message}")
                }
            }
        }

        fun stopPlayback() {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
            mediaPlayer = null
            currentlyPlayingId = null
        }

        override fun onCleared() {
            super.onCleared()
            mStompClient?.disconnect()
            stopPlayback()
        }
    }

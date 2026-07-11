package com.example.canhbao.viewmodel.tinhieu

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canhbao.data.auth.FirebaseTokenProvider
import com.example.canhbao.data.model.hoadon.payment.ThanhToanRequestDTO
import com.example.canhbao.data.model.qua.doiqua.TuiQuaResponseDTO
import com.example.canhbao.data.model.sos.tinhieu.TheoDoiSOSDetailResponseDTO
import com.example.canhbao.data.model.sos.tinhieu.TheoDoiSOSItemResponseDTO
import com.example.canhbao.data.network.BaoCaoSuCoRetrofit.api
import com.example.canhbao.data.network.SocketClientProvider
import com.example.canhbao.data.network.UserSocketManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class TheoDoiTinHieuUiState {
    object Loading : TheoDoiTinHieuUiState()
    data class Success(val data: List<TheoDoiSOSItemResponseDTO>) : TheoDoiTinHieuUiState()
    data class Error(val message: String) : TheoDoiTinHieuUiState()
}

class TheoDoiTinHieuViewModel : ViewModel() {

    var uiState: TheoDoiTinHieuUiState by mutableStateOf(TheoDoiTinHieuUiState.Loading)
        private set

    var listTuiQua by mutableStateOf<List<TuiQuaResponseDTO>>(emptyList())
        private set

    // Quản lý instance UserSocketManager dựa trên file gốc của bạn
    private var userSocketManager: UserSocketManager? = null

    private var mediaPlayer: MediaPlayer? = null

    var currentlyPlayingId by mutableStateOf<Long?>(null)

    var detailUiStateMap by mutableStateOf<Map<Long, TheoDoiSOSDetailResponseDTO>>(emptyMap())
        private set


    fun fetchData() {
        loadDataFromApi()
        loadTuiQua()
        connectWebSocket()
    }

    private fun loadDataFromApi() {
        viewModelScope.launch {
            try {
                uiState = TheoDoiTinHieuUiState.Loading

                val response = withContext(Dispatchers.IO) {
                    val token = FirebaseTokenProvider.getToken()
                    api.getTheoDoiSOS("Bearer $token"
                    )
                }

                uiState = TheoDoiTinHieuUiState.Success(response)
            } catch (e: Exception) {
                uiState = TheoDoiTinHieuUiState.Error(
                    e.localizedMessage ?: "Lỗi kết nối Server"
                )
                Log.e("TheoDoiTinHieu", "loadData error: ${e.message}")
            }
        }
    }

    private fun loadTuiQua() {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    api.getMyGifts(
                        "Bearer ${FirebaseTokenProvider.getToken()}"
                    )
                }
                listTuiQua = response
            } catch (e: Exception) {
                Log.e("TheoDoiTinHieu", "gift error: ${e.message}")
            }
        }
    }

    fun confirmPayment(hoaDonId: Long, quaId: Long?) {
        viewModelScope.launch {
            try {
                val success = withContext(Dispatchers.IO) {

                    api.confirmPayment(
                        "Bearer ${FirebaseTokenProvider.getToken()}",
                        ThanhToanRequestDTO(
                            hoaDonId = hoaDonId,
                            quaId = quaId,
                            phuongThucThanhToan = "MOMO"
                        )
                    ).isSuccessful
                }

                if (success) {
                    fetchData()
                }
            } catch (e: Exception) {
                Log.e("TheoDoiTinHieu", "payment error: ${e.message}")
            }
        }
    }

    fun cancelSOS(sosId: Long) {
        viewModelScope.launch {
            try {
                val success = withContext(Dispatchers.IO) {
                    val token = FirebaseTokenProvider.getToken()
                    api.cancelSOS(
                        "Bearer $token",
                        sosId
                    ).isSuccessful
                }

                if (success) {
                    loadDataFromApi()
                }
            } catch (e: Exception) {
                Log.e("TheoDoiTinHieu", "cancel error: ${e.message}")
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun connectWebSocket() {

        viewModelScope.launch {

            val activeClient =
                SocketClientProvider.ensureConnected()

            if (userSocketManager != null) {
                return@launch
            }

            userSocketManager =
                UserSocketManager(activeClient).apply {

                    subscribe(object : UserSocketManager.Callback {

                        override fun onSosRefresh() {
                            Log.d(
                                "WebSocket_TinHieu",
                                "Nhận cập nhật trạng thái SOS -> reload"
                            )

                            loadDataFromApi()
                        }

                        override fun onHistoryRefresh() {
                            Log.d(
                                "WebSocket_TinHieu",
                                "Refresh lịch sử"
                            )

                            loadDataFromApi()
                        }

                        override fun onPackageRefresh() {}

                        override fun onInvoiceUpdate(json: String) {}

                        override fun onUserStats(json: String) {}

                        override fun onNewInvoice(json: String) {}

                        override fun onPaymentUpdate(json: String) {}
                    })
                }
        }
    }

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

                    setOnCompletionListener {
                        stopPlayback()
                    }
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

    fun fetchDetailSOS(sosId: Long) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {

                    api.getTheoDoiSOSDetail(
                        "Bearer ${FirebaseTokenProvider.getToken()}",
                        sosId
                    )
                }

                withContext(Dispatchers.Main) {
                    val currentMap = detailUiStateMap.toMutableMap()
                    currentMap[sosId] = response
                    detailUiStateMap = currentMap
                }
            } catch (e: Exception) {
                Log.e("TheoDoiTinHieuViewModel", "Lỗi tải chi tiết SOS #$sosId: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        userSocketManager = null
        stopPlayback()
        Log.w("TheoDoiTinHieu", "🧹 Đã dọn dẹp luồng phát Audio và gỡ bỏ tham chiếu Socket Manager.")
    }
}
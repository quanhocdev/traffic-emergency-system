package com.example.canhbao.viewmodel.tinhieu

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canhbao.data.model.hoadon.payment.ThanhToanRequestDTO
import com.example.canhbao.data.model.hoadon.payment.ThanhToanResponseDTO
import com.example.canhbao.data.model.qua.doiqua.TuiQuaResponseDTO
import com.example.canhbao.data.model.sos.tinhieu.TheoDoiSOSDetailResponseDTO
import com.example.canhbao.data.model.sos.tinhieu.TheoDoiSOSItemResponseDTO
import com.example.canhbao.data.network.AppConfig
import com.example.canhbao.data.network.BaoCaoSuCoRetrofit.api
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent

sealed class TheoDoiTinHieuUiState {

    object Loading : TheoDoiTinHieuUiState()

    data class Success(
        val data: List<TheoDoiSOSItemResponseDTO>
    ) : TheoDoiTinHieuUiState()

    data class Error(
        val message: String
    ) : TheoDoiTinHieuUiState()
}

class TheoDoiTinHieuViewModel : ViewModel() {

    var uiState: TheoDoiTinHieuUiState by mutableStateOf(
        TheoDoiTinHieuUiState.Loading
    )
        private set

    var listTuiQua by mutableStateOf<List<TuiQuaResponseDTO>>(emptyList())
        private set

    private var mStompClient: StompClient? = null

    private var mediaPlayer: MediaPlayer? = null

    var currentlyPlayingId by mutableStateOf<Long?>(null)

    private suspend fun getToken(): String {

        val user =
            FirebaseAuth.getInstance()
                .currentUser
                ?: throw Exception("Chưa đăng nhập")

        val tokenResult =
            Tasks.await(
                user.getIdToken(false)
            )

        return "Bearer ${tokenResult.token ?: ""}"
    }

    fun fetchData() {
        loadDataFromApi()
        loadTuiQua()
        connectWebSocket()
    }

    private fun loadDataFromApi() {

        viewModelScope.launch {

            try {

                uiState =
                    TheoDoiTinHieuUiState.Loading

                val response =
                    withContext(Dispatchers.IO) {

                        val token = getToken()

                        api.getTheoDoiSOS(token)
                    }

                uiState =
                    TheoDoiTinHieuUiState.Success(response)

            } catch (e: Exception) {

                uiState =
                    TheoDoiTinHieuUiState.Error(
                        e.localizedMessage
                            ?: "Lỗi kết nối Server"
                    )

                Log.e(
                    "TheoDoiTinHieu",
                    "loadData error: ${e.message}"
                )
            }
        }
    }

    private fun loadTuiQua() {

        viewModelScope.launch {

            try {

                val response =
                    withContext(Dispatchers.IO) {

                        val token = getToken()

                        api.getMyGifts(token)
                    }

                listTuiQua = response

            } catch (e: Exception) {

                Log.e(
                    "TheoDoiTinHieu",
                    "gift error: ${e.message}"
                )
            }
        }
    }

    fun confirmPayment(
        hoaDonId: Long,
        quaId: Long?
    ) {

        viewModelScope.launch {

            try {

                val success =
                    withContext(Dispatchers.IO) {

                        val token = getToken()

                        api.confirmPayment(
                            token,
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

                Log.e(
                    "TheoDoiTinHieu",
                    "payment error: ${e.message}"
                )
            }
        }
    }

    fun cancelSOS(
        sosId: Long
    ) {

        viewModelScope.launch {

            try {

                val success =
                    withContext(Dispatchers.IO) {

                        val token = getToken()

                        api.cancelSOS(
                            token,
                            sosId
                        ).isSuccessful
                    }

                if (success) {
                    loadDataFromApi()
                }

            } catch (e: Exception) {

                Log.e(
                    "TheoDoiTinHieu",
                    "cancel error: ${e.message}"
                )
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun connectWebSocket() {
        if (mStompClient?.isConnected == true) return

        val uid = FirebaseAuth.getInstance().currentUser?.uid

        mStompClient = Stomp.over(
            Stomp.ConnectionProvider.OKHTTP,
            "${AppConfig.WS_BASE_URL}/ws-suco"
        )

        // 🌟 Bọc toàn bộ các lệnh subscribe vào bên trong sự kiện OPENED
        mStompClient?.lifecycle()?.subscribe { lifecycleEvent ->
            when (lifecycleEvent.type) {
                LifecycleEvent.Type.OPENED -> {
                    Log.d("WebSocket_TinHieu", "🟢 Kết nối SOS thành công! Đang đăng ký các kênh bảo mật...")

                    // 1. SỬA KÊNH TRẠNG THÁI SOS: Ẩn danh hóa hoàn toàn, không cộng chuỗi UID nữa
                    mStompClient?.topic("/user/queue/sos-status")?.subscribe({
                        Log.d("WebSocket", "Nhận cập nhật trạng thái SOS riêng tư")
                        loadDataFromApi()
                    }, {
                        Log.e("WebSocket", "SOS error: ${it.message}")
                    })

                    // 2. SỬA KÊNH LỊCH SỬ (HISTORY): Ẩn danh hóa hoàn toàn
                    mStompClient?.topic("/user/queue/history")?.subscribe({
                        Log.d("WebSocket", "Nhận tín hiệu làm mới lịch sử (REFRESH)")
                        loadDataFromApi()
                    }, {
                        Log.e("WebSocket", "History error: ${it.message}")
                    })


                }
                LifecycleEvent.Type.ERROR -> {
                    Log.e("WebSocket_TinHieu", "❌ Lỗi luồng: ${lifecycleEvent.exception?.message}")
                }
                else -> {}
            }
        }

        mStompClient?.connect()
    }


    fun playRecording(
        url: String,
        id: Long
    ) {

        viewModelScope.launch(
            Dispatchers.IO
        ) {

            try {

                if (currentlyPlayingId == id) {

                    stopPlayback()

                    return@launch
                }

                stopPlayback()

                mediaPlayer =
                    MediaPlayer().apply {

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

                Log.e(
                    "MediaPlayer",
                    "Play error: ${e.message}"
                )
            }
        }
    }

    fun stopPlayback() {

        mediaPlayer?.apply {

            if (isPlaying)
                stop()

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


    var detailUiStateMap by mutableStateOf<Map<Long, TheoDoiSOSDetailResponseDTO>>(emptyMap())
        private set

    fun fetchDetailSOS(sosId: Long) {
        viewModelScope.launch {
            try {
                // 💡 GIẢI PHÁP: Đẩy toàn bộ tác vụ lấy Token và gọi API sang IO Thread (Luồng nền)
                val response = withContext(Dispatchers.IO) {
                    val token = getToken() // Chạy an toàn trên luồng nền, không sợ block Main Thread nữa
                    api.getTheoDoiSOSDetail(token, sosId)
                }

                // 🔄 Cập nhật UI State (Phải thực hiện trên Main Thread)
                withContext(Dispatchers.Main) {
                    val currentMap = detailUiStateMap.toMutableMap()
                    currentMap[sosId] = response
                    detailUiStateMap = currentMap
                }

            } catch (e: Exception) {
                Log.e("TheoDoiTinHieuViewModel", "Lỗi tải chi tiết SOS #$sosId: ${e.message}")
                // Bạn có thể bổ sung xử lý UI tại đây nếu muốn, ví dụ: gán một trạng thái lỗi để tắt vòng xoay
            }
        }
    }
}
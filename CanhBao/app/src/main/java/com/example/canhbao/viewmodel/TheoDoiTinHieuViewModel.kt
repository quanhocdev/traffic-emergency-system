package com.example.canhbao.viewmodel

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

    var pendingInvoicesMap by mutableStateOf<Map<Long, ThanhToanResponseDTO>>(emptyMap())
        private set

    var selectedPayment by mutableStateOf<ThanhToanResponseDTO?>(null)
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

        if (mStompClient?.isConnected == true)
            return

        val uid = FirebaseAuth.getInstance().currentUser?.uid

        mStompClient = Stomp.over(
            Stomp.ConnectionProvider.OKHTTP,
            "${AppConfig.WS_BASE_URL}/ws-suco/websocket"
        )

        // SOS status user-specific
        uid?.let {

            mStompClient?.topic("/topic/user/$it/sos-status")
                ?.subscribe({

                    loadDataFromApi()

                }, {
                    Log.e("WebSocket", "SOS error: ${it.message}")
                })
        }

        // history
        uid?.let {

            mStompClient?.topic("/topic/user/$it/history")
                ?.subscribe({

                    loadDataFromApi()

                }, {
                    Log.e("WebSocket", "History error: ${it.message}")
                })
        }

        // invoice
        mStompClient?.topic("/topic/user/invoice")
            ?.subscribe({ topicMessage ->

                val thanhToan =
                    Gson().fromJson(
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

        mStompClient?.connect()
    }

    fun clearInvoice(
        hoaDonId: Long
    ) {

        val currentMap =
            pendingInvoicesMap.toMutableMap()

        currentMap.remove(hoaDonId)

        pendingInvoicesMap =
            currentMap
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
    fun loadPaymentDetail(
        hoaDonId: Long
    ) {

        viewModelScope.launch {

            try {

                val result =
                    withContext(Dispatchers.IO) {

                        val token = getToken()

                        api.getChiTietThanhToan(
                            token,
                            hoaDonId
                        )
                    }

                selectedPayment = result

            } catch (e: Exception) {

                Log.e(
                    "TheoDoiTinHieu",
                    "load payment detail error: ${e.message}"
                )
            }
        }
    }
    fun clearSelectedPayment() {
        selectedPayment = null
    }
    // --- Thêm vào bên trong class TheoDoiTinHieuViewModel ---

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
                android.util.Log.e("TheoDoiTinHieuViewModel", "Lỗi tải chi tiết SOS #$sosId: ${e.message}")
                // Bạn có thể bổ sung xử lý UI tại đây nếu muốn, ví dụ: gán một trạng thái lỗi để tắt vòng xoay
            }
        }
    }
}
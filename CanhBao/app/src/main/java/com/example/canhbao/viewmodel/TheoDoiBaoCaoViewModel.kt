package com.example.canhbao.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canhbao.data.model.suco.baocao.TheoDoiSuCoDetailResponseDTO
import com.example.canhbao.data.network.AppConfig
import com.example.canhbao.data.network.BaoCaoSuCoRetrofit.api
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient

sealed class TheoDoiBaoCaoUiState {
    object Loading : TheoDoiBaoCaoUiState()
    data class Success(
        val data: List<TheoDoiSuCoDetailResponseDTO>
    ) : TheoDoiBaoCaoUiState()

    data class Error(
        val message: String
    ) : TheoDoiBaoCaoUiState()
}

class TheoDoiBaoCaoViewModel : ViewModel() {

    var uiState: TheoDoiBaoCaoUiState by mutableStateOf(
        TheoDoiBaoCaoUiState.Loading
    )
        private set

    private var mStompClient: StompClient? = null

    private suspend fun getToken(): String {
        val user = FirebaseAuth.getInstance().currentUser
            ?: throw Exception("Chưa đăng nhập")

        val tokenResult = Tasks.await(
            user.getIdToken(false)
        )

        return "Bearer ${tokenResult.token ?: ""}"
    }

    fun fetchData() {
        loadDataFromApi()
        connectWebSocket()
    }

    private fun loadDataFromApi() {
        viewModelScope.launch {
            try {

                uiState = TheoDoiBaoCaoUiState.Loading

                val response = withContext(Dispatchers.IO) {
                    val token = getToken()
                    api.getTheoDoiSuCo(token)
                }

                uiState =
                    TheoDoiBaoCaoUiState.Success(response)

            } catch (e: Exception) {

                uiState =
                    TheoDoiBaoCaoUiState.Error(
                        e.localizedMessage
                            ?: "Lỗi kết nối Server"
                    )

                Log.e(
                    "TheoDoiBaoCao",
                    "loadData error: ${e.message}"
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

        // 🌟 Lắng nghe vòng đời kết nối trước để đảm bảo an toàn đường truyền
        mStompClient?.lifecycle()?.subscribe { lifecycleEvent ->
            when (lifecycleEvent.type) {
                ua.naiksoftware.stomp.dto.LifecycleEvent.Type.OPENED -> {
                    Log.d("WebSocket_BaoCao", "🟢 Kết nối thành công! Bắt đầu subscribe...")

                    // Kênh 1: Sự cố chung công khai trên bản đồ
                    mStompClient?.topic("/topic/su-co")?.subscribe({
                        Log.d("WebSocket_BaoCao", "🔄 Nhận tín hiệu sự cố chung -> Tải lại API")
                        loadDataFromApi()
                    }, {
                        Log.e("WebSocket_BaoCao", "Lỗi kênh /topic/su-co: ${it.message}")
                    })

                    // Kênh 2: Lịch sử cá nhân của người báo (Cần ghép UID động giống bên SOS)
                    uid?.let { id ->
                        mStompClient?.topic("/topic/user/$id/history")?.subscribe({
                            Log.d("WebSocket_BaoCao", "🔄 Nhận tín hiệu lịch sử cá nhân -> Tải lại API")
                            loadDataFromApi()
                        }, {
                            Log.e("WebSocket_BaoCao", "Lỗi kênh history cá nhân: ${it.message}")
                        })
                    }
                }
                ua.naiksoftware.stomp.dto.LifecycleEvent.Type.ERROR -> {
                    Log.e("WebSocket_BaoCao", "❌ Lỗi kết nối WebSocket: ${lifecycleEvent.exception?.message}")
                }
                ua.naiksoftware.stomp.dto.LifecycleEvent.Type.CLOSED -> {
                    Log.w("WebSocket_BaoCao", "🔌 Kết nối WebSocket đã đóng")
                }
                else -> {}
            }
        }

        // Kích hoạt kết nối sau khi đã thiết lập lắng nghe vòng đời
        mStompClient?.connect()
    }

    fun cancelBaoCao(
        baoCaoId: Long
    ) {

        viewModelScope.launch {

            try {

                val success =
                    withContext(Dispatchers.IO) {

                        val token = getToken()

                        api.cancelSuCo(
                            token,
                            baoCaoId
                        ).isSuccessful
                    }

                if (success) {
                    loadDataFromApi()
                }

            } catch (e: Exception) {

                Log.e(
                    "TheoDoiBaoCao",
                    "cancel error: ${e.message}"
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mStompClient?.disconnect()
    }
}
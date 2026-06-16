package com.example.canhbao.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canhbao.data.model.suco.baocao.TheoDoiSuCoDetailResponseDTO
import com.example.canhbao.data.model.suco.baocao.TheoDoiSuCoItemResponseDTO // ✅ Thêm import bản rút gọn
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
        val data: List<TheoDoiSuCoItemResponseDTO> // 🔄 ĐÃ SỬA: Danh sách trả về bản Item rút gọn
    ) : TheoDoiBaoCaoUiState()

    data class Error(
        val message: String
    ) : TheoDoiBaoCaoUiState()
}

class TheoDoiBaoCaoViewModel : ViewModel() {

    // Quản lý trạng thái màn hình danh sách sự cố
    var uiState: TheoDoiBaoCaoUiState by mutableStateOf(TheoDoiBaoCaoUiState.Loading)
        private set

    // ➕ BỔ SUNG: Map lưu trữ dữ liệu chi tiết của từng sự cố dựa trên ID (Khớp hoàn toàn với SOS)
    var detailUiStateMap = mutableStateMapOf<Long, TheoDoiSuCoDetailResponseDTO>()
        private set

    private var mStompClient: StompClient? = null

    private suspend fun getToken(): String {
        val user = FirebaseAuth.getInstance().currentUser
            ?: throw Exception("Chưa đăng nhập")

        val tokenResult = Tasks.await(user.getIdToken(false))
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
                    api.getTheoDoiSuCo(token) // Nhận về List<TheoDoiSuCoItemResponseDTO>
                }

                uiState = TheoDoiBaoCaoUiState.Success(response)
            } catch (e: Exception) {
                uiState = TheoDoiBaoCaoUiState.Error(
                    e.localizedMessage ?: "Lỗi kết nối Server"
                )
                Log.e("TheoDoiBaoCao", "loadData error: ${e.message}")
            }
        }
    }

    /**
     * ➕ BỔ SUNG: Hàm lấy dữ liệu chi tiết của một Sự cố cụ thể từ API
     * Sẽ được gọi tự động bên trong `LaunchedEffect` của ChiTietBaoCaoScreen
     */
    fun fetchDetailSuCo(suCoId: Long) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    val token = getToken()
                    api.getTheoDoiSuCoDetail(token, suCoId)
                }
                // Cập nhật hoặc thêm mới đối tượng vào Map theo ID để UI cập nhật realtime
                detailUiStateMap[suCoId] = response
            } catch (e: Exception) {
                Log.e("TheoDoiBaoCao", "fetchDetailSuCo lỗi ID #$suCoId: ${e.message}")
            }
        }
    }

    /**
     * 🔄 ĐÃ SỬA TÊN HÀM: Đổi từ cancelBaoCao thành cancelSuCo để khớp 100% với
     * nút bấm "Hủy Báo Cáo Sự Cố" bên giao diện ChiTietBaoCaoScreen bạn vừa viết
     */
    fun cancelSuCo(suCoId: Long) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    val token = getToken()
                    api.cancelSuCo(token, suCoId)
                    // Lưu ý: Nếu api.cancelSuCo trả về Response<Void>, bạn có thể dùng .isSuccessful
                    // Nếu nó trả về trực tiếp DTO hoặc kiểu khác, hãy cấu trúc lại dòng này cho khớp Retrofit nhé
                }

                // Hủy thành công thì xóa dữ liệu cũ trong map chi tiết và refresh lại danh sách tổng
                detailUiStateMap.remove(suCoId)
                loadDataFromApi()
            } catch (e: Exception) {
                Log.e("TheoDoiBaoCao", "cancel error: ${e.message}")
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

        mStompClient?.lifecycle()?.subscribe { lifecycleEvent ->
            when (lifecycleEvent.type) {
                ua.naiksoftware.stomp.dto.LifecycleEvent.Type.OPENED -> {
                    Log.d("WebSocket_BaoCao", "🟢 Kết nối thành công! Bắt đầu subscribe...")

                    // Kênh 1: Có sự cố mới công cộng chung
                    mStompClient?.topic("/topic/su-co")?.subscribe({
                        Log.d("WebSocket_BaoCao", "🔄 Nhận tín hiệu sự cố chung -> Tải lại API")
                        loadDataFromApi()
                    }, {
                        Log.e("WebSocket_BaoCao", "Lỗi kênh /topic/su-co: ${it.message}")
                    })

                    // Kênh 2: Có cập nhật trạng thái trong lịch sử cá nhân (Ví dụ: Trạm bấm tiếp nhận/Từ chối)
                    uid?.let { id ->
                        mStompClient?.topic("/topic/user/$id/history")?.subscribe({
                            Log.d("WebSocket_BaoCao", "🔄 Nhận tín hiệu lịch sử cá nhân -> Tải lại API")
                            loadDataFromApi()

                            // 💡 Mẹo thông minh: Khi có update lịch sử từ socket, quét qua các ID đang mở
                            // trên màn hình chi tiết để kéo lại data mới nhất luôn, không sợ bị out-date dữ liệu
                            detailUiStateMap.keys.forEach { activeId ->
                                fetchDetailSuCo(activeId)
                            }
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

        mStompClient?.connect()
    }

    override fun onCleared() {
        super.onCleared()
        mStompClient?.disconnect()
    }
}
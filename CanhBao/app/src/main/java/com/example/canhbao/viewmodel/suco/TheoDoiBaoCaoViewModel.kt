package com.example.canhbao.viewmodel.suco

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canhbao.data.auth.FirebaseTokenProvider
import com.example.canhbao.data.model.suco.baocao.TheoDoiSuCoDetailResponseDTO
import com.example.canhbao.data.model.suco.baocao.TheoDoiSuCoItemResponseDTO
import com.example.canhbao.data.network.BaoCaoSuCoRetrofit.api
import com.example.canhbao.data.network.PublicSocketManager
import com.example.canhbao.data.network.SocketClientProvider
import com.example.canhbao.data.network.UserSocketManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class TheoDoiBaoCaoUiState {
    object Loading : TheoDoiBaoCaoUiState()
    data class Success(val data: List<TheoDoiSuCoItemResponseDTO>) : TheoDoiBaoCaoUiState()
    data class Error(val message: String) : TheoDoiBaoCaoUiState()
}

class TheoDoiBaoCaoViewModel : ViewModel() {

    // Quản lý trạng thái màn hình danh sách sự cố
    var uiState: TheoDoiBaoCaoUiState by mutableStateOf(TheoDoiBaoCaoUiState.Loading)
        private set

    // Map lưu trữ dữ liệu chi tiết của từng sự cố
    var detailUiStateMap = mutableStateMapOf<Long, TheoDoiSuCoDetailResponseDTO>()
        private set

    // Quản lý 2 luồng Socket riêng biệt
    private var publicSocketManager: PublicSocketManager? = null
    private var userSocketManager: UserSocketManager? = null



    fun fetchData() {
        loadDataFromApi()
        connectWebSocket()
    }

    private fun loadDataFromApi() {
        viewModelScope.launch {
            try {
                uiState = TheoDoiBaoCaoUiState.Loading

                val response = withContext(Dispatchers.IO) {
                    val token = FirebaseTokenProvider.getToken()
                    api.getTheoDoiSuCo("Bearer $token")
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
     * Hàm lấy dữ liệu chi tiết của một Sự cố cụ thể từ API
     * Sẽ được gọi tự động bên trong `LaunchedEffect` của ChiTietBaoCaoScreen
     */
    fun fetchDetailSuCo(suCoId: Long) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    val token = FirebaseTokenProvider.getToken()
                    api.getTheoDoiSuCoDetail("Bearer $token", suCoId)
                }
                detailUiStateMap[suCoId] = response
            } catch (e: Exception) {
                Log.e("TheoDoiBaoCao", "fetchDetailSuCo lỗi ID #$suCoId: ${e.message}")
            }
        }
    }

    /**
     * Hủy báo cáo sự cố
     */
    fun cancelSuCo(suCoId: Long) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val token = FirebaseTokenProvider.getToken()
                    api.cancelSuCo("Bearer $token", suCoId)
                }

                detailUiStateMap.remove(suCoId)
                loadDataFromApi()
            } catch (e: Exception) {
                Log.e("TheoDoiBaoCao", "cancel error: ${e.message}")
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun connectWebSocket() {

        viewModelScope.launch {

            val activeClient = SocketClientProvider.ensureConnected()

            if (
                publicSocketManager != null &&
                userSocketManager != null
            ) {
                return@launch
            }

            publicSocketManager = null
            userSocketManager = null

            // PUBLIC
            publicSocketManager = PublicSocketManager(activeClient).apply {

                subscribe(object : PublicSocketManager.Callback {

                    override fun onSuCoUpdate(json: String) {
                        Log.d("WebSocket_BaoCao", "Nhận tín hiệu sự cố chung -> reload")
                        loadDataFromApi()
                    }

                    override fun onSuCoDelete(id: Long) {
                        Log.d("WebSocket_BaoCao", "Xóa sự cố $id")
                        detailUiStateMap.remove(id)
                        loadDataFromApi()
                    }

                    override fun onTruSoUpdate(json: String) {}
                    override fun onTruSoDelete(id: Long) {}
                    override fun onCameraUpdate(json: String) {}
                    override fun onCameraDelete(id: Long) {}
                    override fun onPublicFundUpdate(json: String) {}
                })
            }

            // USER
            userSocketManager = UserSocketManager(activeClient).apply {

                subscribe(object : UserSocketManager.Callback {

                    override fun onHistoryRefresh() {

                        Log.d(
                            "WebSocket_BaoCao",
                            " Refresh lịch sử"
                        )

                        loadDataFromApi()

                        detailUiStateMap.keys.forEach {
                            fetchDetailSuCo(it)
                        }
                    }

                    override fun onPackageRefresh() {}
                    override fun onSosRefresh() {}
                    override fun onInvoiceUpdate(json: String) {}
                    override fun onUserStats(json: String) {}
                    override fun onNewInvoice(json: String) {}
                    override fun onPaymentUpdate(json: String) {}
                })
            }
        }
    }
    override fun onCleared() {
        super.onCleared()
        publicSocketManager = null
        userSocketManager = null
        Log.w("WebSocket_BaoCao", "🧹 Đã giải phóng bộ đôi Public và User Socket Manager trong TheoDoiBaoCaoViewModel")
    }
}
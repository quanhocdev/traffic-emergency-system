package com.example.canhbao.viewmodel.tienich

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canhbao.data.auth.FirebaseTokenProvider
import com.example.canhbao.data.model.suco.baocao.SuCoUserDto
import com.example.canhbao.data.model.tien.quyengop.QuyenGopRequestDTO
import com.example.canhbao.data.model.tien.quyengop.QuyenGopResponseDTO
import com.example.canhbao.data.network.BaoCaoSuCoRetrofit
import com.example.canhbao.data.network.SocketClientProvider
import com.example.canhbao.data.network.UserSocketManager
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class QuyenGopViewModel : ViewModel() {

    var userDetail by mutableStateOf<SuCoUserDto?>(null)
        private set

    var lichSu by mutableStateOf<List<QuyenGopResponseDTO>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var message by mutableStateOf("")
        private set

    private var userSocketManager: UserSocketManager? = null

    private val gson = Gson()

    fun init() {
        fetchUserInfo()
        fetchLichSu()
        connectSocket()
    }

    private fun fetchUserInfo() {
        viewModelScope.launch {
            try {
                val token = FirebaseTokenProvider.getToken()

                userDetail =
                    BaoCaoSuCoRetrofit.api.getUserInfo("Bearer $token")

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchLichSu() {
        viewModelScope.launch {
            try {

                val token = FirebaseTokenProvider.getToken()

                lichSu =
                    BaoCaoSuCoRetrofit.api.getLichSuQuyenGop(
                        "Bearer $token"
                    )

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun thucHienQuyenGop(
        soDiem: Int,
        noiDung: String
    ) {
        viewModelScope.launch {
            isLoading = true
            try {
                val token = FirebaseTokenProvider.getToken()
                val response =
                    BaoCaoSuCoRetrofit.api.thucHienQuyenGop(
                        "Bearer $token",
                        QuyenGopRequestDTO(
                            soDiemQuyenGop = soDiem,
                            noiDung = noiDung
                        )
                    )

                // Nhận GiaoDichResultDTO từ backend trả về
                if (response.isSuccessful && response.body()?.success == true) {

                    // 1. Trừ điểm nóng trên UI ngay tức khắc
                    userDetail?.let { current ->
                        userDetail = current.copy(
                            totalPoints = (current.totalPoints - soDiem).coerceAtLeast(0)
                        )
                    }

                    // 2. Hiện message từ Object JSON của Backend
                    message = response.body()?.message ?: "Quyên góp thành công"

                    // 3. Đồng bộ lại dữ liệu chuẩn ngầm
                    fetchUserInfo()
                    fetchLichSu()

                    resetMessage()
                } else {
                    // Trường hợp success = false hoặc có lỗi validate
                    message = response.body()?.message
                        ?: response.errorBody()?.string()
                                ?: "Quyên góp thất bại"
                }

            } catch (e: Exception) {
                Log.e("QuyenGopVM", "Lỗi: ${e.message}")
                message = "Có lỗi xảy ra trong quá trình kết nối"
            } finally {
                isLoading = false
            }
        }
    }
    private fun connectSocket() {

        viewModelScope.launch {

            val client =
                SocketClientProvider.ensureConnected()

            if (userSocketManager != null)
                return@launch

            userSocketManager =
                UserSocketManager(client).apply {

                    subscribe(object : UserSocketManager.Callback {

                        override fun onUserStats(json: String) {

                            try {

                                userDetail =
                                    gson.fromJson(
                                        json,
                                        SuCoUserDto::class.java
                                    )

                                Log.d(
                                    "QuyenGopVM",
                                    "User updated realtime"
                                )

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                        }

                        override fun onHistoryRefresh() {
                            fetchLichSu()
                        }

                        override fun onPackageRefresh() {}

                        override fun onSosRefresh() {}

                        override fun onInvoiceUpdate(json: String) {}

                        override fun onNewInvoice(json: String) {}

                        override fun onPaymentUpdate(json: String) {}

                    })

                }

        }

    }

    private fun resetMessage() {

        viewModelScope.launch {

            delay(3000)

            message = ""

        }

    }

    override fun onCleared() {

        super.onCleared()

        userSocketManager = null

    }

}
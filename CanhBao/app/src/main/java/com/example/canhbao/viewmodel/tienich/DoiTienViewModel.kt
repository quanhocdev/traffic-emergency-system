package com.example.canhbao.viewmodel.tienich

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canhbao.data.auth.FirebaseTokenProvider
import com.example.canhbao.data.model.suco.baocao.SuCoUserDto
import com.example.canhbao.data.model.tien.DoiTienRequestDTO
import com.example.canhbao.data.model.tien.DoiTienResultDTO
import com.example.canhbao.data.model.tien.ThongKeQuyDto
import com.example.canhbao.data.network.BaoCaoSuCoRetrofit
import com.example.canhbao.data.network.PublicSocketManager
import com.example.canhbao.data.network.SocketClientProvider
import com.example.canhbao.data.network.UserSocketManager
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class FilterMode { DAY, MONTH, YEAR }

class DoiTienViewModel : ViewModel() {
    var userDetail by mutableStateOf<SuCoUserDto?>(null)
        private set

    var isProcessing by mutableStateOf(false)
        private set

    var message by mutableStateOf("")
        private set

    var publicFundStats by mutableStateOf(ThongKeQuyDto())
        private set

    private var userSocketManager: UserSocketManager? = null
    private var publicSocketManager: PublicSocketManager? = null
    private val gson = Gson()



    // Khai báo các biến state mới cho việc lọc
    var filterMode by mutableStateOf(FilterMode.DAY)
    var selectedDate by mutableStateOf(LocalDate.now())

    val filteredVinhDanh get() = publicFundStats.bangVinhDanh

    // Tính tổng tiền theo danh sách đã lọc
    val totalFilteredValue get() = filteredVinhDanh.sumOf { it.giaTri}

    // Tiến tới 1 đơn vị thời gian (Ngày/Tháng/Năm)
    fun nextPeriod() {
        selectedDate = when (filterMode) {
            FilterMode.DAY -> selectedDate.plusDays(1)
            FilterMode.MONTH -> selectedDate.plusMonths(1)
            FilterMode.YEAR -> selectedDate.plusYears(1)
        }
    }

    // Lùi lại 1 đơn vị thời gian
    fun prevPeriod() {
        selectedDate = when (filterMode) {
            FilterMode.DAY -> selectedDate.minusDays(1)
            FilterMode.MONTH -> selectedDate.minusMonths(1)
            FilterMode.YEAR -> selectedDate.minusYears(1)
        }
    }

    fun init() {
        fetchUserInfo()
        fetchPublicFund() // Lấy dữ liệu quỹ hiện tại từ database
        connectSockets()
    }

    private fun fetchPublicFund() {
        viewModelScope.launch {
            try {
                val stats = BaoCaoSuCoRetrofit.api.getThongKeQuy()
                publicFundStats = stats
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun fetchUserInfo() {
        viewModelScope.launch {
            try {
                val token = FirebaseTokenProvider.getToken()
                userDetail = BaoCaoSuCoRetrofit.api.getUserInfo("Bearer $token")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun connectSockets() {

        viewModelScope.launch {

            val activeClient = SocketClientProvider.ensureConnected()

            if (
                userSocketManager != null &&
                publicSocketManager != null
            ) {
                return@launch
            }

            userSocketManager = null
            publicSocketManager = null

            // USER SOCKET
            userSocketManager = UserSocketManager(activeClient).apply {

                subscribe(object : UserSocketManager.Callback {

                    override fun onUserStats(json: String) {
                        try {
                            userDetail =
                                gson.fromJson(json, SuCoUserDto::class.java)

                            Log.d(
                                "WebSocket_DoiTien",
                                "🟢 Đã đồng bộ điểm cá nhân mới"
                            )

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onHistoryRefresh() {}
                    override fun onPackageRefresh() {}
                    override fun onSosRefresh() {}
                    override fun onInvoiceUpdate(json: String) {}
                    override fun onNewInvoice(json: String) {}
                    override fun onPaymentUpdate(json: String) {}
                })
            }

            // PUBLIC SOCKET
            publicSocketManager = PublicSocketManager(activeClient).apply {

                subscribe(object : PublicSocketManager.Callback {

                    override fun onPublicFundUpdate(json: String) {
                        try {

                            publicFundStats =
                                gson.fromJson(
                                    json,
                                    ThongKeQuyDto::class.java
                                )

                            Log.d(
                                "WebSocket_DoiTien",
                                "🟢 Tổng quỹ công khai biến động realtime"
                            )

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onSuCoUpdate(json: String) {}
                    override fun onSuCoDelete(id: Long) {}
                    override fun onTruSoUpdate(json: String) {}
                    override fun onTruSoDelete(id: Long) {}
                    override fun onCameraUpdate(json: String) {}
                    override fun onCameraDelete(id: Long) {}
                })
            }
        }
    }
    // Hàm xử lý chung cho cả Đổi tiền và Quyên góp
    fun thucHienGiaoDich(points: Int, type: String) {
        viewModelScope.launch {
            isProcessing = true
            try {

                val token = FirebaseTokenProvider.getToken()

                val dto = DoiTienRequestDTO(
                    soDiemDoi = points,
                    loaiDoi = type
                )

                val response = BaoCaoSuCoRetrofit.api.thucHienDoiTien(
                    "Bearer $token",
                    dto
                )

                if (response.isSuccessful) {

                    message = response.body()?.message ?: "Giao dịch thành công!"

                    fetchUserInfo()

                } else {

                    message = response.errorBody()?.string()
                        ?: "Giao dịch thất bại!"
                }

            } catch (e: Exception) {
                message = "Lỗi kết nối: ${e.message}"
            } finally {
                isProcessing = false
                resetMessage()
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
        publicSocketManager = null
        Log.w("WebSocket_DoiTien", "🧹 Đã giải phóng bộ đôi Socket Managers trong DoiTienViewModel")
    }
}
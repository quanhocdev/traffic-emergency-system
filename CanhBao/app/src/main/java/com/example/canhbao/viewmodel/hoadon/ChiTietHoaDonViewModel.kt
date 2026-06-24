package com.example.canhbao.viewmodel.hoadon

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canhbao.data.model.hoadon.HoaDonUserResponseDTO
import com.example.canhbao.data.network.BaoCaoSuCoRetrofit
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.annotation.SuppressLint
import com.example.canhbao.data.network.AppConfig
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent

class ChiTietHoaDonViewModel : ViewModel() {

    var hoaDon by mutableStateOf<HoaDonUserResponseDTO?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private var mStompClient: StompClient? = null

    private var currentHoaDonId: Long? = null

    private var subscribed = false
    private suspend fun getToken(): String {

        val user =
            FirebaseAuth.getInstance()
                .currentUser
                ?: throw Exception("Chưa đăng nhập")

        val tokenResult =
            Tasks.await(
                user.getIdToken(false)
            )

        return "Bearer ${tokenResult.token}"
    }

    @SuppressLint("CheckResult")
    private fun connectWebSocket() {

        if (mStompClient?.isConnected == true)
            return

        mStompClient = Stomp.over(
            Stomp.ConnectionProvider.OKHTTP,
            "${AppConfig.WS_BASE_URL}/ws-suco"
        )

        mStompClient?.lifecycle()?.subscribe { event ->

            when (event.type) {

                LifecycleEvent.Type.OPENED -> {

                    Log.d(
                        "HoaDonSocket",
                        "Connected"
                    )

                    if (subscribed) return@subscribe

                    subscribed = true

                    mStompClient?.topic(
                        "/user/queue/new-invoice"
                    )?.subscribe({

                        Log.d(
                            "HoaDonSocket",
                            "Nhận hóa đơn mới"
                        )

                        currentHoaDonId?.let {
                            loadHoaDonDetail(it)
                        }

                    }, {

                        Log.e(
                            "HoaDonSocket",
                            it.message ?: ""
                        )
                    })
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
    fun loadHoaDonDetail(
        hoaDonId: Long
    ) {

        currentHoaDonId = hoaDonId

        if (mStompClient?.isConnected != true) {
            connectWebSocket()
        }

        viewModelScope.launch {

            try {

                Log.d(
                    "ChiTietHoaDon",
                    "STEP 1 - Start loadHoaDonDetail"
                )

                isLoading = true
                errorMessage = null

                val token =
                    withContext(Dispatchers.IO) {

                        Log.d(
                            "ChiTietHoaDon",
                            "STEP 2 - Getting token"
                        )

                        getToken()
                    }

                Log.d(
                    "ChiTietHoaDon",
                    "STEP 3 - Got token"
                )

                hoaDon =
                    withContext(Dispatchers.IO) {

                        Log.d(
                            "ChiTietHoaDon",
                            "STEP 4 - Calling getHoaDonDetail"
                        )

                        BaoCaoSuCoRetrofit.api.getHoaDonDetail(
                            token,
                            hoaDonId
                        )
                    }

                Log.d(
                    "ChiTietHoaDon",
                    "STEP 5 - Loaded hoaDon"
                )

                Log.d(
                    "ChiTietHoaDon",
                    "STEP 7 - Finished"
                )

            } catch (e: Exception) {

                Log.e(
                    "ChiTietHoaDon",
                    "MAIN ERROR",
                    e
                )

                errorMessage =
                    e.message ?: "Lỗi tải dữ liệu"

            } finally {

                isLoading = false

                Log.d(
                    "ChiTietHoaDon",
                    "STEP 8 - End"
                )
            }
        }
    }
}
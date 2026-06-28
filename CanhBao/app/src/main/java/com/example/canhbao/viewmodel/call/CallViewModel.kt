package com.example.canhbao.viewmodel.call

import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canhbao.data.network.SocketClientProvider
import com.example.canhbao.data.network.UserSocketManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import org.json.JSONObject

class CallViewModel : ViewModel() {

    private var userSocketManager: UserSocketManager? = null
    private var isListening = false // Biến cờ ngăn chặn subscribe trùng lặp nhiều lần

    fun start(
        context: Context,
        webrtcViewModel: WebRTCViewModel
    ) {
        if (isListening) return

        val userId = FirebaseAuth.getInstance().currentUser?.uid
            ?: Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )

        Log.w(
            "WebRTC_Debug",
            "Android đăng ký lắng nghe Call qua UserSocketManager"
        )

        viewModelScope.launch {

            val activeClient = SocketClientProvider.ensureConnected()

            userSocketManager = UserSocketManager(activeClient).apply {

                subscribe(object : UserSocketManager.Callback {

                    override fun onPaymentUpdate(json: String) {
                        try {
                            val jsonObject = JSONObject(json)
                            webrtcViewModel.handleSignal(jsonObject, userId)
                        } catch (e: Exception) {
                            Log.e(
                                "WebRTC_Debug",
                                "Lỗi định tuyến tín hiệu: ${e.message}"
                            )
                        }
                    }

                    override fun onHistoryRefresh() {}
                    override fun onPackageRefresh() {}
                    override fun onSosRefresh() {}
                    override fun onInvoiceUpdate(json: String) {}
                    override fun onUserStats(json: String) {}
                    override fun onNewInvoice(json: String) {}
                })
            }

            isListening = true
        }
    }

    fun resetListener() {
        isListening = false
    }

    override fun onCleared() {
        super.onCleared()
        userSocketManager = null
        Log.w("WebRTC_Debug", "🧹 Đã giải phóng UserSocketManager trong CallViewModel")
    }
}
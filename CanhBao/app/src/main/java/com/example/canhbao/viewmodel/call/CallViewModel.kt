package com.example.canhbao.viewmodel.call

import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.canhbao.data.network.SocketClientProvider
import com.example.canhbao.data.network.UserSocketManager
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONObject

class CallViewModel : ViewModel() {

    private var userSocketManager: UserSocketManager? = null
    private var isListening = false // Biến cờ ngăn chặn subscribe trùng lặp nhiều lần

    fun start(
        context: Context,
        webrtcViewModel: WebRTCViewModel
    ) {
        val activeClient = SocketClientProvider.stompClient
        Log.d("WebRTC_Debug", "Hàm start() được gọi. Trạng thái kết nối Socket trung tâm: ${activeClient.isConnected}")

        if (isListening && activeClient.isConnected) {
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
            ?: Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

        Log.w("WebRTC_Debug", "Android đăng ký lắng nghe luồng sự kiện Call qua UserSocketManager tập trung")

        // Khởi tạo và liên kết trực tiếp với luồng callback WebRTC thông qua cổng queue bảo mật của User
        userSocketManager = UserSocketManager(activeClient).apply {
            subscribe(object : UserSocketManager.Callback {

                override fun onPaymentUpdate(json: String) {
                    try {
                        val jsonObject = JSONObject(json)
                        // Định tuyến gói tin signal thô trực tiếp sang cho WebRTCViewModel xử lý
                        webrtcViewModel.handleSignal(jsonObject, userId)
                    } catch (e: Exception) {
                        Log.e("WebRTC_Debug", "Lỗi định tuyến tín hiệu cuộc gọi: ${e.message}")
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

        // Kích hoạt kết nối Client nếu chưa chạy
        if (!activeClient.isConnected) {
            activeClient.connect()
        }

        isListening = activeClient.isConnected
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
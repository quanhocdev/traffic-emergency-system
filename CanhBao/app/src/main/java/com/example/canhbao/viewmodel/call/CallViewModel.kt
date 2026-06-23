package com.example.canhbao.viewmodel.call

import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import ua.naiksoftware.stomp.StompClient

class CallViewModel : ViewModel() {

    private val socket = CallSocketManager()
    private var isListening = false // Biến cờ ngăn chặn subscribe trùng lặp nhiều lần

    fun start(
        context: Context,
        stompClient: StompClient,
        webrtcViewModel: WebRTCViewModel
    ) {
        // Log kiểm tra xem hàm này có chạy qua khi Socket đã mở chưa
        Log.d("WebRTC_Debug", "Hàm start() được gọi. Trạng thái kết nối Socket: ${stompClient.isConnected}")

        if (isListening && stompClient.isConnected) {
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
            ?: Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

        Log.w("WebRTC_Debug", "Android bắt đầu SUBSCRIBE đường dẫn: /topic/user/$userId/call")

        socket.listen(stompClient, userId) { json ->
            webrtcViewModel.handleSignal(json, stompClient, userId)
        }

        isListening = stompClient.isConnected
    }
    fun resetListener() {
        isListening = false
    }
}
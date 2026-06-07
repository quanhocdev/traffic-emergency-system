package com.example.canhbao.data.network

import android.util.Log
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient

object SocketClientProvider {

    private var _stompClient: StompClient? = null

    val stompClient: StompClient
        get() {
            if (_stompClient == null) {
                initNewClient()
            }
            return _stompClient!!
        }

    fun initNewClient() {
        Log.w("SOCKET_DEBUG", "🧹 Đang dọn dẹp và tạo mới instance Stomp Client tránh rác bộ nhớ...")
        try {
            _stompClient?.disconnect() // Ngắt kết nối cũ hoàn toàn nếu có
        } catch (e: Exception) { e.printStackTrace() }

        _stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, AppConfig.WS_PURE_URL).apply {
            withClientHeartbeat(20000)
            withServerHeartbeat(20000)
        }
    }
}
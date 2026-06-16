package com.example.canhbao.data.network

import android.util.Log
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient

object SocketClientProvider {

    private var _stompClient: StompClient? = null

    // Lấy instance hiện tại, nếu chưa có thì mới tạo, tránh việc tạo đè liên tục
    val stompClient: StompClient
        get() {
            if (_stompClient == null) {
                initNewClient()
            }
            return _stompClient!!
        }

    fun initNewClient() {
        // Kỹ thuật an toàn: Nếu client cũ đang kết nối tốt, tuyệt đối KHÔNG khởi tạo lại làm sập luồng
        if (_stompClient != null && _stompClient!!.isConnected) {
            Log.d("SOCKET_DEBUG", "✅ Instance Stomp cũ vẫn đang kết nối tốt. Bỏ qua lệnh tạo mới.")
            return
        }

        Log.w("SOCKET_DEBUG", "🧹 Tiến hành dọn dẹp sạch luồng cũ và cấu hình cổng mạng mới...")

        try {
            // Chỉ disconnect khi thực sự có instance cũ đang chạy dở dang
            _stompClient?.disconnect()
        } catch (e: Exception) {
            Log.e("SOCKET_DEBUG", "❌ Lỗi dọn dẹp kết nối: ${e.message}")
        }

        // TỐI ƯU OKHTTP: Tăng timeout để tránh rớt mạng mạng LAN/WiFi thất thường
        val customOkHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        // Khởi tạo instance mới độc lập hoàn toàn
        _stompClient = Stomp.over(
            Stomp.ConnectionProvider.OKHTTP,
            AppConfig.WS_PURE_URL,
            null,
            customOkHttpClient
        ).apply {
            // Heartbeat giúp giữ kết nối sống (Ping/Pong giữa Android và Spring Boot)
            withClientHeartbeat(15000)
            withServerHeartbeat(15000)
        }
    }
}
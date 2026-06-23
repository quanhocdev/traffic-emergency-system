package com.example.canhbao.viewmodel.call

import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import ua.naiksoftware.stomp.StompClient

class CallSocketManager {
    fun listen(
        stompClient: StompClient,
        userId: String,
        onSignal: (JSONObject) -> Unit
    ) {
        Log.d("WebRTC_Socket", "Bắt đầu subscribe hòm thư cuộc gọi cho user: $userId")

        stompClient.topic("/topic/user/$userId/call")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ message ->
                try {
                    Log.d("WebRTC_Socket", "Nhận được payload thô: ${message.payload}")
                    val json = JSONObject(message.payload)
                    onSignal(json)
                } catch (e: Exception) {
                    Log.e("WebRTC_Socket", "Lỗi phân tích cú pháp JSON tín hiệu cuộc gọi: ${e.message}")
                }
            }, { throwable ->
                Log.e("WebRTC_Socket", "Lỗi kết nối hoặc đứt Subscription của đường dẫn cuộc gọi", throwable)
            })
    }
}
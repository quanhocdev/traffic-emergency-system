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
    private var isListening = false

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

        Log.w("WebRTC_Debug", "Start Call listener")

        viewModelScope.launch {

            // ⚠️ CHỈ LẤY SOCKET ĐÃ CONNECT, KHÔNG CONNECT Ở ĐÂY
            val client = SocketClientProvider.stompClient

            userSocketManager = UserSocketManager(client).apply {

                subscribe(object : UserSocketManager.Callback {

                    override fun onPaymentUpdate(json: String) {
                        try {
                            val obj = JSONObject(json)
                            webrtcViewModel.handleSignal(obj, userId)
                        } catch (e: Exception) {
                            Log.e("WebRTC_Debug", "Parse error ${e.message}")
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
    }
}
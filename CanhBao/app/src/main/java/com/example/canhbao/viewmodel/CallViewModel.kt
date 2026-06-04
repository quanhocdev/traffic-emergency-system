package com.example.canhbao.viewmodel

import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import ua.naiksoftware.stomp.StompClient
import org.json.JSONObject

class CallViewModel : ViewModel() {

    private val socket = CallSocketManager()

    fun start(
        context: Context,
        stompClient: StompClient,
        webrtcViewModel: WebRTCViewModel
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
            ?: Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

        Log.i("WebRTC", "Listening: /topic/user/$userId/call")

        socket.listen(stompClient, userId) { json ->
            webrtcViewModel.handleSignal(json, stompClient, userId)
        }
    }
}
package com.example.canhbao.viewmodel

import org.json.JSONObject
import ua.naiksoftware.stomp.StompClient


class CallSocketManager {
    fun listen(
        stompClient: StompClient,
        userId: String,
        onSignal: (JSONObject) -> Unit
    ) {
        stompClient.topic("/topic/user/$userId/call")
            .subscribe({ message ->
                onSignal(JSONObject(message.payload))
            }, { it.printStackTrace() })
    }
}
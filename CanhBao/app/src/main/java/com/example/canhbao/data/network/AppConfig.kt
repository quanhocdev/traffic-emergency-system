package com.example.canhbao.data.network

object AppConfig {
    const val BASE_IP = "192.168.1.13"
    const val PORT = 8080

    const val HTTP_BASE_URL = "http://$BASE_IP:$PORT"
    const val WS_BASE_URL = "ws://$BASE_IP:$PORT"
}
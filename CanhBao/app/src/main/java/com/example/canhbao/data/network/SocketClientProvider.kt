package com.example.canhbao.data.network

import android.util.Log
import com.example.canhbao.data.auth.FirebaseTokenProvider
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.StompHeader


object SocketClientProvider {


    private var _stompClient: StompClient? = null

    private var currentToken: String? = null



    val stompClient: StompClient
        get() {
            return _stompClient
                ?: throw IllegalStateException(
                    "Socket chưa init"
                )
        }




    fun initNewClient(token: String) {


        /*
        ==================================
        Nếu socket tồn tại và cùng token
        => dùng lại
        ==================================
        */

        if (
            _stompClient != null &&
            currentToken == token
        ) {

            Log.d(
                "SOCKET_DEBUG",
                "Reuse socket cũ"
            )

            return
        }




        /*
        ==================================
        Token đổi hoặc chưa có socket
        => tạo mới
        ==================================
        */


        try {

            _stompClient?.disconnect()

            Log.d(
                "SOCKET_DEBUG",
                "Disconnect socket cũ"
            )

        } catch(e:Exception){

            Log.e(
                "SOCKET_DEBUG",
                "Disconnect lỗi ${e.message}"
            )
        }




        val customOkHttpClient =
            OkHttpClient.Builder()

                .connectTimeout(
                    10,
                    TimeUnit.SECONDS
                )

                .readTimeout(
                    0,
                    TimeUnit.SECONDS
                )

                .writeTimeout(
                    10,
                    TimeUnit.SECONDS
                )

                .build()





        _stompClient =
            Stomp.over(
                Stomp.ConnectionProvider.OKHTTP,
                AppConfig.WS_PURE_URL,
                null,
                customOkHttpClient
            )
                .apply {


                    withClientHeartbeat(
                        15000
                    )


                    withServerHeartbeat(
                        15000
                    )



                    connect(
                        listOf(

                            StompHeader(
                                "Authorization",
                                "Bearer $token"
                            )

                        )
                    )

                }



        currentToken = token



        Log.d(
            "SOCKET_DEBUG",
            "Tạo socket mới"
        )
    }


    suspend fun ensureConnected(): StompClient {

        val token = FirebaseTokenProvider.getToken()

        initNewClient(token)

        return stompClient
    }


    fun disconnect(){

        try {

            _stompClient?.disconnect()

            _stompClient = null

            currentToken = null


            Log.d(
                "SOCKET_DEBUG",
                "Socket disconnected"
            )


        } catch(e:Exception){

            Log.e(
                "SOCKET_DEBUG",
                "Disconnect error ${e.message}"
            )
        }
    }

}
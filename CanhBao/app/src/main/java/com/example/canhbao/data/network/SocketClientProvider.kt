package com.example.canhbao.data.network

import android.util.Log
import com.example.canhbao.data.auth.FirebaseTokenProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import okhttp3.OkHttpClient
import kotlinx.coroutines.suspendCancellableCoroutine
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume


object SocketClientProvider {


    private var _stompClient: StompClient? = null

    private var currentToken: String? = null



    val stompClient: StompClient
        get() =
            _stompClient
                ?: throw IllegalStateException(
                    "Socket chưa init"
                )




    fun initNewClient(token:String){


        if(
            _stompClient != null &&
            currentToken == token
        ){

            Log.d(
                "SOCKET_DEBUG",
                "Reuse socket"
            )

            return
        }




        try {

            _stompClient?.disconnect()

        }catch(e:Exception){}



        val okHttpClient =
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



        val client =
            Stomp.over(
                Stomp.ConnectionProvider.OKHTTP,
                AppConfig.WS_PURE_URL,
                null,
                okHttpClient
            )


        client.withClientHeartbeat(15000)

        client.withServerHeartbeat(15000)



        client.connect(
            listOf(
                StompHeader(
                    "Authorization",
                    "Bearer $token"
                )
            )
        )


        _stompClient = client

        currentToken = token



        Log.d(
            "SOCKET_DEBUG",
            "Create socket"
        )

    }





    suspend fun ensureConnected():StompClient{


        val token =
            FirebaseTokenProvider.getToken()



        initNewClient(token)



        return suspendCancellableCoroutine { cont ->



            stompClient.lifecycle()
                .observeOn(
                    AndroidSchedulers.mainThread()
                )
                .subscribe {


                    when(it.type){


                        LifecycleEvent.Type.OPENED -> {


                            Log.d(
                                "SOCKET_DEBUG",
                                "CONNECTED"
                            )


                            if(cont.isActive){

                                cont.resume(
                                    stompClient
                                )

                            }

                        }


                        LifecycleEvent.Type.ERROR -> {

                            Log.e(
                                "SOCKET_DEBUG",
                                "SOCKET ERROR"
                            )

                        }


                        else -> {}

                    }

                }

        }

    }




    fun disconnect(){


        try {


            _stompClient?.disconnect()

            _stompClient=null

            currentToken=null


            Log.d(
                "SOCKET_DEBUG",
                "disconnect"
            )


        }catch(e:Exception){


            Log.e(
                "SOCKET_DEBUG",
                e.message ?: ""
            )

        }

    }

}
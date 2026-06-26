package com.example.canhbao.viewmodel.camera

import android.util.Log
import com.example.canhbao.data.model.info.camera.CameraMapDto
import com.example.canhbao.data.network.SocketClientProvider
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ua.naiksoftware.stomp.StompClient


class CameraSocket(
    private val stompClient: StompClient =
        SocketClientProvider.stompClient
) {


    private val gson = Gson()


    interface Callback {

        fun onCameraUpdate(
            camera: CameraMapDto
        )


        fun onCameraDelete(
            id: Long
        )

    }




    fun subscribe(
        callback: Callback
    ) {


        // ================= CAMERA UPDATE =================


        stompClient.topic(
            "/topic/camera"
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                try {

                    val camera =
                        gson.fromJson(
                            it.payload,
                            CameraMapDto::class.java
                        )


                    callback.onCameraUpdate(
                        camera
                    )


                } catch(e:Exception){

                    Log.e(
                        "CameraSocket",
                        "Parse lỗi ${e.message}"
                    )

                }


            }, {

                Log.e(
                    "CameraSocket",
                    "Socket camera lỗi ${it.message}"
                )

            })





        // ================= CAMERA DELETE =================


        stompClient.topic(
            "/topic/camera-delete"
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                it.payload
                    .toLongOrNull()
                    ?.let { id ->

                        callback.onCameraDelete(
                            id
                        )

                    }


            }, {

                Log.e(
                    "CameraSocket",
                    "Delete camera lỗi ${it.message}"
                )

            })

    }


}
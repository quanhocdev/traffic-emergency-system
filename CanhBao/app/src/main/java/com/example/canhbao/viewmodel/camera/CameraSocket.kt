package com.example.canhbao.viewmodel.camera

import android.util.Log
import com.example.canhbao.data.model.info.camera.CameraMapDto
import com.example.canhbao.data.network.SocketClientProvider
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class CameraSocket {


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


        val client =
            SocketClientProvider.stompClient



        client.topic("/topic/camera")
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
                        e.message ?: ""
                    )

                }


            },{


                Log.e(
                    "CameraSocket",
                    it.message ?: ""
                )


            })





        client.topic("/topic/camera-delete")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({


                it.payload
                    .toLongOrNull()
                    ?.let(callback::onCameraDelete)



            },{


                Log.e(
                    "CameraSocket",
                    it.message ?: ""
                )


            })



    }

}
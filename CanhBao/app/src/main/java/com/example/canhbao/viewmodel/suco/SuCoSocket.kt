package com.example.canhbao.viewmodel.suco

import android.util.Log
import com.example.canhbao.data.model.suco.baocao.SuCoMapResponseDTO
import com.example.canhbao.data.network.SocketClientProvider
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class SuCoSocket {


    interface Callback {

        fun onSuCoUpdate(
            suCo: SuCoMapResponseDTO
        )


        fun onSuCoDelete(
            id: Long
        )
    }



    private val gson = Gson()



    fun subscribe(
        callback: Callback
    ){


        val client =
            SocketClientProvider.stompClient



        // ===== UPDATE =====

        client.topic("/topic/su-co")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                try {

                    val suCo =
                        gson.fromJson(
                            it.payload,
                            SuCoMapResponseDTO::class.java
                        )


                    callback.onSuCoUpdate(
                        suCo
                    )


                } catch(e:Exception){

                    Log.e(
                        "SuCoSocket",
                        "Parse lỗi ${e.message}"
                    )
                }


            },{

                Log.e(
                    "SuCoSocket",
                    "Socket lỗi ${it.message}"
                )

            })





        // ===== DELETE =====


        client.topic("/topic/su-co-delete")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({


                val id =
                    it.payload.toLongOrNull()


                if(id != null){

                    callback.onSuCoDelete(
                        id
                    )

                }


            },{


                Log.e(
                    "SuCoSocket",
                    "Delete lỗi ${it.message}"
                )

            })

    }





    fun start(){

        val client =
            SocketClientProvider.stompClient


        if(!client.isConnected){

            client.connect()

        }


        Log.d(
            "SuCoSocket",
            "SuCo socket started"
        )
    }

}
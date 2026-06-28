package com.example.canhbao.viewmodel.truso

import android.util.Log
import com.example.canhbao.data.model.info.truso.TruSoMapDto
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ua.naiksoftware.stomp.StompClient


class TruSoSocket {


    private val gson = Gson()


    interface Callback {

        fun onTruSoUpdate(
            truSo: TruSoMapDto
        )

        fun onTruSoDelete(
            id: Long
        )
    }



    private val stompClient: StompClient
        get() = com.example.canhbao.data.network.SocketClientProvider.stompClient




    fun subscribe(
        callback: Callback
    ) {


        val client = stompClient



        client.topic("/topic/tru-so")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                try {

                    val truSo =
                        gson.fromJson(
                            it.payload,
                            TruSoMapDto::class.java
                        )


                    callback.onTruSoUpdate(truSo)


                } catch(e:Exception){

                    Log.e(
                        "TruSoSocket",
                        e.message ?: ""
                    )
                }


            },{

                Log.e(
                    "TruSoSocket",
                    it.message ?: ""
                )

            })







        client.topic("/topic/tru-so-delete")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({


                it.payload
                    .toLongOrNull()
                    ?.let {

                            id ->
                        callback.onTruSoDelete(id)

                    }


            },{

                Log.e(
                    "TruSoSocket",
                    it.message ?: ""
                )

            })



        Log.d(
            "TruSoSocket",
            "Subscribe trụ sở"
        )

    }


}
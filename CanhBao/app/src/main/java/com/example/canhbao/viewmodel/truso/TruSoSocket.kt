package com.example.canhbao.viewmodel.truso

import android.util.Log
import com.example.canhbao.data.model.info.truso.TruSoMapDto
import com.example.canhbao.data.network.SocketClientProvider
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ua.naiksoftware.stomp.StompClient


class TruSoSocket(
    private val stompClient: StompClient =
        SocketClientProvider.stompClient
) {


    private val gson = Gson()


    interface Callback {

        fun onTruSoUpdate(
            truSo: TruSoMapDto
        )


        fun onTruSoDelete(
            id: Long
        )

    }



    fun subscribe(
        callback: Callback
    ) {


        // ================= TRỤ SỞ UPDATE =================


        stompClient.topic(
            "/topic/tru-so"
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                try {

                    val truSo =
                        gson.fromJson(
                            it.payload,
                            TruSoMapDto::class.java
                        )


                    callback.onTruSoUpdate(
                        truSo
                    )


                } catch (e: Exception) {

                    Log.e(
                        "TruSoSocket",
                        "Parse lỗi: ${e.message}"
                    )

                }


            }, {

                Log.e(
                    "TruSoSocket",
                    "TruSo socket error: ${it.message}"
                )

            })




        // ================= TRỤ SỞ DELETE ================

        stompClient.topic(
            "/topic/tru-so-delete"
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                it.payload
                    .toLongOrNull()
                    ?.let { id ->

                        callback.onTruSoDelete(
                            id
                        )

                    }
            }, {
                Log.e(
                    "TruSoSocket",
                    "Delete error: ${it.message}"
                )
            })
    }
}
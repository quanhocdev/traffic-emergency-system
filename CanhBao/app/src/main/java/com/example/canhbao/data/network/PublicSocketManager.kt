package com.example.canhbao.data.network

import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ua.naiksoftware.stomp.StompClient


class PublicSocketManager(
    private val stompClient: StompClient
) {


    interface Callback {


        fun onSuCoUpdate(json: String)


        fun onSuCoDelete(id: Long)



        fun onTruSoUpdate(json: String)


        fun onTruSoDelete(id: Long)



        fun onCameraUpdate(json: String)


        fun onCameraDelete(id: Long)



        fun onPublicFundUpdate(json: String)

    }




    fun subscribe(callback: Callback) {



        // ================= SỰ CỐ =================


        stompClient.topic("/topic/su-co")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                callback.onSuCoUpdate(
                    it.payload
                )


            }, {

                Log.e(
                    "PublicSocket",
                    "SuCo error ${it.message}"
                )

            })





        // ================= DELETE SỰ CỐ =================


        stompClient.topic("/topic/su-co-delete")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                it.payload.toLongOrNull()
                    ?.let { id ->

                        callback.onSuCoDelete(id)

                    }


            }, {

                Log.e(
                    "PublicSocket",
                    "SuCo delete error ${it.message}"
                )

            })







        // ================= TRỤ SỞ =================



        stompClient.topic("/topic/tru-so")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                callback.onTruSoUpdate(
                    it.payload
                )


            }, {

                Log.e(
                    "PublicSocket",
                    "TruSo error ${it.message}"
                )

            })






        stompClient.topic("/topic/tru-so-delete")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                it.payload.toLongOrNull()
                    ?.let { id ->

                        callback.onTruSoDelete(id)

                    }


            }, {

                Log.e(
                    "PublicSocket",
                    "TruSo delete error ${it.message}"
                )

            })








        // ================= CAMERA =================



        stompClient.topic("/topic/camera")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                callback.onCameraUpdate(
                    it.payload
                )


            }, {

                Log.e(
                    "PublicSocket",
                    "Camera error ${it.message}"
                )

            })





        stompClient.topic("/topic/camera-delete")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                it.payload.toLongOrNull()
                    ?.let { id ->

                        callback.onCameraDelete(id)

                    }


            }, {

                Log.e(
                    "PublicSocket",
                    "Camera delete error ${it.message}"
                )

            })







        // ================= QUỸ CÔNG KHAI =================



        stompClient.topic("/topic/public-fund")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                callback.onPublicFundUpdate(
                    it.payload
                )


            }, {

                Log.e(
                    "PublicSocket",
                    "Fund error ${it.message}"
                )

            })




        Log.d(
            "PublicSocket",
            "Subscribed all public topics"
        )

    }

}
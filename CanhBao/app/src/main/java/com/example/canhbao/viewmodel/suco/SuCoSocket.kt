package com.example.canhbao.viewmodel.suco

import android.util.Log
import com.example.canhbao.data.model.suco.baocao.SuCoMapResponseDTO
import com.example.canhbao.data.network.SocketClientProvider
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ua.naiksoftware.stomp.StompClient


class SuCoSocket {


    private val disposables = CompositeDisposable()

    private val gson = Gson()



    interface Callback {

        fun onSuCoUpdate(
            suCo: SuCoMapResponseDTO
        )

        fun onSuCoDelete(
            id: Long
        )
    }




    fun subscribe(
        callback: Callback
    ){

        val client: StompClient =
            SocketClientProvider.stompClient



        /*
        ==========================
        UPDATE SỰ CỐ
        ==========================
        */

        val updateDisposable =
            client.topic("/topic/su-co")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                    try {
                        Log.d(
                            "SUCO_SOCKET_RAW",
                            it.payload
                        )
                        val suCo =
                            gson.fromJson(
                                it.payload,
                                SuCoMapResponseDTO::class.java
                            )


                        Log.d(
                            "SUCO_SOCKET",
                            "RECEIVE ${suCo.id} ${suCo.mucDoSuCo}"
                        )


                        callback.onSuCoUpdate(
                            suCo
                        )


                    }catch(e:Exception){

                        Log.e(
                            "SUCO_SOCKET",
                            "Parse error ${e.message}"
                        )
                    }


                },{
                    Log.e(
                        "SUCO_SOCKET",
                        "Update error ${it.message}"
                    )
                })


        disposables.add(updateDisposable)






        /*
        ==========================
        DELETE SỰ CỐ
        ==========================
        */


        val deleteDisposable =
            client.topic("/topic/su-co-delete")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                    it.payload
                        .toLongOrNull()
                        ?.let { id ->

                            Log.d(
                                "SUCO_SOCKET",
                                "DELETE $id"
                            )

                            callback.onSuCoDelete(
                                id
                            )
                        }


                },{
                    Log.e(
                        "SUCO_SOCKET",
                        "Delete error ${it.message}"
                    )
                })



        disposables.add(deleteDisposable)





        Log.d(
            "SUCO_SOCKET",
            "Subscribed /topic/su-co"
        )

    }





    fun clear(){

        disposables.clear()

        Log.d(
            "SUCO_SOCKET",
            "Socket disposable cleared"
        )
    }

}
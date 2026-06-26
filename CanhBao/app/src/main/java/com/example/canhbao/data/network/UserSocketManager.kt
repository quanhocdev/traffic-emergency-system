package com.example.canhbao.data.network

import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ua.naiksoftware.stomp.StompClient

class UserSocketManager(
    private val stompClient: StompClient
) {

    interface Callback {

        fun onHistoryRefresh()

        fun onPackageRefresh()

        fun onSosRefresh()

        fun onInvoiceUpdate(json: String)

        fun onUserStats(json: String)

        fun onNewInvoice(json: String)

        fun onPaymentUpdate(json: String)
    }

    fun subscribe(callback: Callback) {


        // ================= HISTORY =================

        stompClient.topic("/user/queue/history")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                Log.d(
                    "UserSocket",
                    "History refresh"
                )

                callback.onHistoryRefresh()

            }, {

                Log.e(
                    "UserSocket",
                    "History error: ${it.message}"
                )

            })



        // ================= PACKAGE =================


        stompClient.topic("/user/queue/package-status")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                Log.d(
                    "UserSocket",
                    "Package refresh"
                )

                callback.onPackageRefresh()


            }, {

                Log.e(
                    "UserSocket",
                    "Package error: ${it.message}"
                )

            })




        // ================= SOS =================


        stompClient.topic("/user/queue/sos-status")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                Log.d(
                    "UserSocket",
                    "SOS refresh"
                )

                callback.onSosRefresh()


            }, {

                Log.e(
                    "UserSocket",
                    "SOS error: ${it.message}"
                )

            })

        stompClient.topic("/user/queue/invoice")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                callback.onInvoiceUpdate(
                    it.payload
                )


            }, {

                Log.e(
                    "UserSocket",
                    "Invoice error: ${it.message}"
                )

            })


        // ================= NEW INVOICE =================


        stompClient.topic("/user/queue/new-invoice")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                callback.onNewInvoice(
                    it.payload
                )


            }, {

                Log.e(
                    "UserSocket",
                    "New invoice error: ${it.message}"
                )

            })






        // ================= PAYMENT =================


        stompClient.topic("/user/queue/payment")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                callback.onPaymentUpdate(
                    it.payload
                )


            }, {

                Log.e(
                    "UserSocket",
                    "Payment error: ${it.message}"
                )

            })





        // ================= USER STATS =================


        stompClient.topic("/user/queue/user-stats")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({

                callback.onUserStats(
                    it.payload
                )


            }, {

                Log.e(
                    "UserSocket",
                    "UserStats error: ${it.message}"
                )

            })



        Log.d(
            "UserSocket",
            "Subscribed all user topics"
        )
    }
}
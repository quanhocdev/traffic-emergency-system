package com.example.canhbao.viewmodel.truso

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canhbao.data.model.info.truso.TruSoMapDto
import com.example.canhbao.data.network.BaoCaoSuCoApi
import com.example.canhbao.data.network.BaoCaoSuCoRetrofit
import com.example.canhbao.ui.screens.createTruSoMarkerBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.viewModelScope
import com.example.canhbao.data.network.SocketClientProvider
import kotlinx.coroutines.launch


class TruSoViewModel(
    private val context: Context
) : ViewModel() {


    private val suCoApi: BaoCaoSuCoApi =
        BaoCaoSuCoRetrofit.api



    private val socket =
        TruSoSocket()



    private val _truSoWithIcons =
        MutableStateFlow<List<Pair<TruSoMapDto, Bitmap>>>(
            emptyList()
        )

    val truSoWithIcons =
        _truSoWithIcons.asStateFlow()



    private val _showTruSo =
        MutableStateFlow(true)

    val showTruSo =
        _showTruSo.asStateFlow()



    init {

        startSocket()

    }



    // ================= API =================


    fun loadTruSoData() {

        viewModelScope.launch {

            try {

                val list =
                    suCoApi.getAllTruSo()


                val icon =
                    createTruSoMarkerBitmap(
                        context
                    )


                _truSoWithIcons.value =
                    list.map {
                        it to icon
                    }


                Log.d(
                    "TruSoViewModel",
                    "Load ${list.size} trụ sở"
                )


            } catch(e:Exception){

                Log.e(
                    "TruSoViewModel",
                    e.message ?: ""
                )

            }

        }

    }



    private fun startSocket(){


        viewModelScope.launch {


            try {


                // đảm bảo socket tồn tại
                SocketClientProvider
                    .ensureConnected()



                socket.subscribe(

                    object : TruSoSocket.Callback {


                        override fun onTruSoUpdate(
                            truSo: TruSoMapDto
                        ) {


                            updateTruSoFromSocket(
                                truSo
                            )

                        }



                        override fun onTruSoDelete(
                            id: Long
                        ) {


                            removeTruSoFromSocket(
                                id
                            )

                        }


                    }

                )



                Log.d(
                    "TruSoViewModel",
                    "Socket trụ sở started"
                )



            } catch(e:Exception){


                Log.e(
                    "TruSoViewModel",
                    "Socket lỗi ${e.message}"
                )

            }

        }

    }



    private fun updateTruSoFromSocket(
        truSo: TruSoMapDto
    ) {


        viewModelScope.launch(
            Dispatchers.IO
        ) {


            val icon =
                createTruSoMarkerBitmap(
                    context
                )


            val current =
                _truSoWithIcons.value
                    .toMutableList()



            current.removeAll {

                it.first.id == truSo.id

            }


            current.add(
                truSo to icon
            )



            withContext(
                Dispatchers.Main
            ){

                _truSoWithIcons.value =
                    current.toList()

            }


            Log.d(
                "REALTIME",
                "Update trụ sở ${truSo.id}"
            )

        }

    }





    private fun removeTruSoFromSocket(
        id: Long
    ){

        viewModelScope.launch(
            Dispatchers.Main
        ){

            _truSoWithIcons.value =
                _truSoWithIcons.value
                    .filter {
                        it.first.id != id
                    }


            Log.d(
                "REALTIME",
                "Delete trụ sở $id"
            )

        }

    }


    fun toggleFilter(){

        _showTruSo.value =
            !_showTruSo.value

    }


}
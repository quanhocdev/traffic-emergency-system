package com.example.canhbao.viewmodel.camera

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canhbao.data.model.info.camera.CameraMapDto
import com.example.canhbao.data.network.BaoCaoSuCoApi
import com.example.canhbao.data.network.BaoCaoSuCoRetrofit
import com.example.canhbao.data.network.SocketClientProvider
import com.example.canhbao.ui.screens.createCameraIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CameraViewModel(
    private val context: Context
) : ViewModel() {



    private val suCoApi: BaoCaoSuCoApi =
        BaoCaoSuCoRetrofit.api



    private val socket =
        CameraSocket()



    private val _cameraWithIcons =
        MutableStateFlow<List<Pair<CameraMapDto, Bitmap>>>(
            emptyList()
        )

    val cameraWithIcons =
        _cameraWithIcons.asStateFlow()



    private val _showCamera =
        MutableStateFlow(true)

    val showCamera =
        _showCamera.asStateFlow()




    init {

        startSocket()

    }





    // ================= API =================


    fun loadCameraData() {

        viewModelScope.launch {

            try {

                val list =
                    suCoApi.getAllCamera()


                val icon =
                    createCameraIcon(
                        context
                    )


                _cameraWithIcons.value =
                    list.map {
                        it to icon
                    }



                Log.d(
                    "CameraViewModel",
                    "Load ${list.size} camera"
                )


            } catch(e:Exception){

                Log.e(
                    "CameraViewModel",
                    e.message ?: ""
                )

            }

        }

    }






    // ================= SOCKET =================
    private fun startSocket(){


        viewModelScope.launch {


            try {


                // tạo socket nếu chưa có
                SocketClientProvider
                    .ensureConnected()



                socket.subscribe(

                    object : CameraSocket.Callback {


                        override fun onCameraUpdate(
                            camera: CameraMapDto
                        ) {


                            updateCameraFromSocket(
                                camera
                            )

                        }



                        override fun onCameraDelete(
                            id: Long
                        ) {


                            removeCameraFromSocket(
                                id
                            )

                        }


                    }

                )



                Log.d(
                    "CameraViewModel",
                    "Camera socket started"
                )


            } catch(e:Exception){


                Log.e(
                    "CameraViewModel",
                    e.message ?: ""
                )


            }

        }

    }





    private fun updateCameraFromSocket(
        camera: CameraMapDto
    ){


        viewModelScope.launch(
            Dispatchers.IO
        ){

            try {


                val icon =
                    createCameraIcon(
                        context
                    )


                val current =
                    _cameraWithIcons.value
                        .toMutableList()



                current.removeAll {

                    it.first.id == camera.id

                }


                current.add(
                    camera to icon
                )



                withContext(
                    Dispatchers.Main
                ){

                    _cameraWithIcons.value =
                        current.toList()

                }


                Log.d(
                    "REALTIME",
                    "Update camera ${camera.id}"
                )


            }catch(e:Exception){

                Log.e(
                    "CameraViewModel",
                    e.message ?: ""
                )

            }


        }

    }





    private fun removeCameraFromSocket(
        id:Long
    ){

        viewModelScope.launch(
            Dispatchers.Main
        ){

            _cameraWithIcons.value =
                _cameraWithIcons.value
                    .filter {

                        it.first.id != id

                    }


            Log.d(
                "REALTIME",
                "Delete camera $id"
            )

        }

    }





    fun toggleFilter(){

        _showCamera.value =
            !_showCamera.value

    }


}
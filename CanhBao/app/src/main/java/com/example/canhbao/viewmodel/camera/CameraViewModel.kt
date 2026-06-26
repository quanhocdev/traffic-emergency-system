package com.example.canhbao.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canhbao.data.model.info.camera.CameraMapDto
import com.example.canhbao.data.network.BaoCaoSuCoApi
import com.example.canhbao.data.network.BaoCaoSuCoRetrofit
import com.example.canhbao.ui.screens.createCameraIcon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CameraViewModel : ViewModel() {

    private val suCoApi: BaoCaoSuCoApi =
        BaoCaoSuCoRetrofit.api

    private val _cameraWithIcons =
        MutableStateFlow<List<Pair<CameraMapDto, Bitmap>>>(emptyList())

    val cameraWithIcons =
        _cameraWithIcons.asStateFlow()

    private val _showCamera =
        MutableStateFlow(true)

    val showCamera =
        _showCamera.asStateFlow()

    fun loadCameraData(context: Context) {

        viewModelScope.launch {
            try {
                val list =
                    suCoApi.getAllCamera()
                val icon =
                    createCameraIcon(context)

                _cameraWithIcons.value =
                    list.map {
                        it to icon
                    }

                Log.d(
                    "CameraViewModel",
                    "Load ${list.size} camera"
                )
            } catch (e: Exception) {

                Log.e(
                    "CameraViewModel",
                    "Load camera lỗi ${e.message}"
                )
            }
        }
    }
    fun toggleFilter() {

        _showCamera.value =
            !_showCamera.value

    }






    // Hàm được gọi từ PublicSocketManager

    fun updateCameraFromSocket(
        context: Context,
        camera: CameraMapDto
    ) {

        val icon =
            createCameraIcon(context)

        val current =
            _cameraWithIcons.value.toMutableList()

        // nếu camera tồn tại thì thay thế
        current.removeAll {
            it.first.id == camera.id
        }

        // thêm mới
        current.add(
            camera to icon
        )
        _cameraWithIcons.value =
            current.toList()

        Log.d(
            "CameraViewModel",
            "Realtime update camera ${camera.id}"
        )
    }

    fun removeCameraFromSocket(
        id: Long
    ) {
        _cameraWithIcons.value =
            _cameraWithIcons.value.filter {
                it.first.id != id
            }

        Log.d(
            "CameraViewModel",
            "Delete camera $id"
        )
    }


}
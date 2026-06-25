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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CameraViewModel : ViewModel() {

    private val suCoApi: BaoCaoSuCoApi = BaoCaoSuCoRetrofit.api

    private val _cameraWithIcons = MutableStateFlow<List<Pair<CameraMapDto, Bitmap>>>(emptyList())
    val cameraWithIcons = _cameraWithIcons.asStateFlow()

    private val _showCamera = MutableStateFlow(true)
    val showCamera = _showCamera.asStateFlow()

    fun loadCameraData(context: Context) {
        viewModelScope.launch {
            try {
                val list = suCoApi.getAllCamera()
                val icon = createCameraIcon(context)
                _cameraWithIcons.value = list.map { it to icon }
                Log.d("CameraViewModel", "📸 Đã load ${list.size} camera.")
            } catch (e: Exception) { Log.e("CameraViewModel", "❌ Lỗi load camera: ${e.message}") }
        }
    }

    fun toggleFilter() {
        _showCamera.value = !_showCamera.value
    }

    fun updateCameraFromSocket(context: Context, camera: CameraMapDto) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("REALTIME_BUG", "🔄 CameraViewModel xử lý Camera (ID: ${camera.id})")
            val icon = createCameraIcon(context)
            val current = _cameraWithIcons.value.toMutableList()
            current.removeAll { it.first.id == camera.id }
            current.add(camera to icon)

            withContext(Dispatchers.Main) {
                _cameraWithIcons.value = current.toList()
            }
        }
    }

    fun removeCameraFromSocket(id: Long) {
        viewModelScope.launch(Dispatchers.Main) {
            val current = _cameraWithIcons.value.filter { it.first.id != id }
            _cameraWithIcons.value = current
            Log.w("REALTIME_BUG", "📸 CameraViewModel đã xóa camera ID = $id")
        }
    }
}
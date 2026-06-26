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
                Log.d("CameraViewModel", "Load ${list.size} camera thành công từ API")
            } catch (e: Exception) {
                Log.e("CameraViewModel", "Load camera lỗi: ${e.message}")
            }
        }
    }

    fun toggleFilter() {
        _showCamera.value = !_showCamera.value
    }

    /**
     * Được gọi trực tiếp sau khi MapViewModel nhận và parse JSON từ PublicSocketManager
     * Đẩy việc xử lý danh sách và tạo Bitmap sang IO Thread để bảo toàn hiệu năng UI
     */
    fun updateCameraFromSocket(context: Context, camera: CameraMapDto) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("CameraViewModel", "Realtime nhận cập nhật camera từ Socket (ID: ${camera.id})")
                val icon = createCameraIcon(context)
                val current = _cameraWithIcons.value.toMutableList()

                // Nếu camera đã tồn tại, tiến hành xóa bản cũ để cập nhật đè bản mới
                current.removeAll { it.first.id == camera.id }
                current.add(camera to icon)

                // Cập nhật lại State Flow đồng bộ trên Main Thread
                withContext(Dispatchers.Main) {
                    _cameraWithIcons.value = current.toList()
                }
            } catch (e: Exception) {
                Log.e("CameraViewModel", "Lỗi update camera từ socket: ${e.message}")
            }
        }
    }

    fun removeCameraFromSocket(id: Long) {
        viewModelScope.launch(Dispatchers.Main) {
            val updatedList = _cameraWithIcons.value.filter { it.first.id != id }
            _cameraWithIcons.value = updatedList
            Log.w("CameraViewModel", "Realtime đã xóa camera ID = $id khỏi bản đồ")
        }
    }
}
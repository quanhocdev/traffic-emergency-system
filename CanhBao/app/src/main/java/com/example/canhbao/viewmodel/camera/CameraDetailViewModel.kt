package com.example.canhbao.viewmodel.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canhbao.data.model.camera.CameraMapDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CameraDetailViewModel : ViewModel() {

    private val _detail = MutableStateFlow<CameraMapDto?>(null)
    val detail = _detail.asStateFlow()

    fun setDetail(data: CameraMapDto) {
        viewModelScope.launch {
            _detail.value = data
        }
    }

    fun clear() {
        _detail.value = null
    }
}
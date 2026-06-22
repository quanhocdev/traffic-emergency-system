package com.example.canhbao.viewmodel.truso

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canhbao.data.model.truso.TruSoMapDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TruSoDetailViewModel : ViewModel() {

    private val _detail = MutableStateFlow<TruSoMapDto?>(null)
    val detail = _detail.asStateFlow()

    fun setDetail(data: TruSoMapDto) {
        viewModelScope.launch {
            _detail.value = data
        }
    }

    fun clear() {
        _detail.value = null
    }
}
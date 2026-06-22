package com.example.canhbao.viewmodel.suco

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canhbao.data.model.suco.baocao.UserSuCoDetailResponseDTO
import com.example.canhbao.data.network.BaoCaoSuCoApi
import com.example.canhbao.data.network.BaoCaoSuCoRetrofit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SuCoDetailViewModel : ViewModel() {

    private val api: BaoCaoSuCoApi = BaoCaoSuCoRetrofit.api

    private val _detail = MutableStateFlow<UserSuCoDetailResponseDTO?>(null)
    val detail = _detail.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadDetail(id: Long) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                _detail.value = api.getSuCoDetail(id)

            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun clear() {
        _detail.value = null
        _error.value = null
    }
}
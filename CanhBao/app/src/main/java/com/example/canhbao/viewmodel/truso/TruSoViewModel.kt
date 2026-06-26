package com.example.canhbao.viewmodel

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

class TruSoViewModel : ViewModel() {

    private val suCoApi: BaoCaoSuCoApi = BaoCaoSuCoRetrofit.api

    private val _truSoWithIcons = MutableStateFlow<List<Pair<TruSoMapDto, Bitmap>>>(emptyList())
    val truSoWithIcons = _truSoWithIcons.asStateFlow()

    private val _showTruSo = MutableStateFlow(true)
    val showTruSo = _showTruSo.asStateFlow()

    fun loadTruSoData(context: Context) {
        viewModelScope.launch {
            try {
                val list = suCoApi.getAllTruSo()
                val icon = createTruSoMarkerBitmap(context)
                _truSoWithIcons.value = list.map { it to icon }
                Log.d("TruSoViewModel", "Load API: Đã lấy ${list.size} trụ sở")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun toggleFilter() {
        _showTruSo.value = !_showTruSo.value
    }
    // Được gọi trực tiếp sau khi MapViewModel nhận và parse JSON từ PublicSocketManager
    fun updateTruSoFromSocket(context: Context, truSo: TruSoMapDto) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("REALTIME_BUG", "TruSoViewModel xử lý Trụ sở từ Socket (ID: ${truSo.id})")
            val icon = createTruSoMarkerBitmap(context)
            val current = _truSoWithIcons.value.toMutableList()
            current.removeAll { it.first.id == truSo.id }
            current.add(truSo to icon)

            withContext(Dispatchers.Main) {
                _truSoWithIcons.value = current.toList()
            }
        }
    }
    // Được gọi trực tiếp khi MapViewModel bắt được sự kiện onTruSoDelete(id) từ PublicSocketManager
    fun removeTruSoFromSocket(id: Long) {
        viewModelScope.launch(Dispatchers.Main) {
            val updatedTruSo = _truSoWithIcons.value.filter { it.first.id != id }
            _truSoWithIcons.value = updatedTruSo
            Log.w("REALTIME_BUG", "TruSoViewModel đã xóa trụ sở ID = $id khỏi bản đồ")
        }
    }
}
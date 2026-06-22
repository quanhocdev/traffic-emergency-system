package com.example.canhbao.viewmodel.helper

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canhbao.data.model.suco.baocao.SuCoMapResponseDTO
import com.mapbox.geojson.Point
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlertViewModel : ViewModel() {

    // bật/tắt âm thanh
    private val _isSoundEnabled = MutableStateFlow(true)
    val isSoundEnabled = _isSoundEnabled.asStateFlow()

    // gửi câu lệnh đọc ra UI
    private val _ttsCommand = MutableSharedFlow<String>()
    val ttsCommand = _ttsCommand

    // trạng thái cảnh báo theo từng sự cố
    private val alertLevels = mutableMapOf<Long, Int>()

    fun toggleSound() {
        _isSoundEnabled.value = !_isSoundEnabled.value
    }

    fun checkProximityAndAlert(
        userPoint: Point,
        suCoList: List<SuCoMapResponseDTO> // Đã đổi sang DTO mới ở đây
    ) {
        if (suCoList.isEmpty()) return

        viewModelScope.launch {
            suCoList.forEach { suCo ->

                val results = FloatArray(2)

                Location.distanceBetween(
                    userPoint.latitude(), userPoint.longitude(),
                    suCo.viDo, suCo.kinhDo,
                    results
                )

                val distance = results[0]
                val bearing = results[1]
                val currentLevel = alertLevels[suCo.id] ?: 0

                val roundedDistance = distance.toInt()

                // Vì SuCoMapResponseDTO không có tên cụ thể, ta dùng định danh chung là "sự cố"
                val tenSuCo = "sự cố"

                val mucDoText = when (suCo.mucDoSuCo?.uppercase()) {
                    "HIGH" -> "mức độ cao"
                    "MEDIUM" -> "mức độ trung bình"
                    "LOW" -> "mức độ thấp"
                    else -> ""
                }

                val messageBase =
                    if (mucDoText.isEmpty()) "có $tenSuCo"
                    else "có $tenSuCo $mucDoText"

                when {
                    distance <= 100f && currentLevel < 1 -> {
                        if (_isSoundEnabled.value) {
                            _ttsCommand.emit("Chú ý! Cách $roundedDistance mét $messageBase")
                        }
                        alertLevels[suCo.id] = 1
                    }

                    distance <= 30f && currentLevel < 2 -> {
                        if (_isSoundEnabled.value) {
                            _ttsCommand.emit("Cảnh báo! Chỉ còn $roundedDistance mét $messageBase")
                        }
                        alertLevels[suCo.id] = 2
                    }

                    distance > 150f -> {
                        alertLevels.remove(suCo.id)
                    }
                }
            }
        }
    }
}
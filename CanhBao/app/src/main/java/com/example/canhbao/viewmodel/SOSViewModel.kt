package com.example.canhbao.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

class SOSViewModel : ViewModel() {

    private val _sosResponse = MutableStateFlow<String?>(null)
    val sosResponse = _sosResponse.asStateFlow()

    private val _currentHandlingTruSoId = MutableStateFlow<String?>(null)
    val currentHandlingTruSoId = _currentHandlingTruSoId.asStateFlow()

    fun handleSosResponse(jsonRaw: String) {
        try {
            val json = JSONObject(jsonRaw)

            val message = json.optString("message")
            val sosId = json.optString("sosId")

            _sosResponse.value = message
            _currentHandlingTruSoId.value = sosId

            Log.d("SOS", "message=$message sosId=$sosId")

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clear() {
        _sosResponse.value = null
    }
}
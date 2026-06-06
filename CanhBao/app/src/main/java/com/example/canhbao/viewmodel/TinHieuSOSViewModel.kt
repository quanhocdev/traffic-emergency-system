package com.example.canhbao.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canhbao.data.model.sos.tinhieu.TinHieuSOSRequestDTO
import com.example.canhbao.data.network.BaoCaoSuCoRetrofit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import android.location.Geocoder
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

class TinHieuSOSViewModel : ViewModel() {

    private val _currentStep = MutableStateFlow(1)
    val currentStep = _currentStep.asStateFlow()

    var isSubmitting by mutableStateOf(false)
    var statusSOS by mutableStateOf("")

    var recordedFile by mutableStateOf<File?>(null)
    var noteText by mutableStateOf("")

    fun resetState() {
        _currentStep.value = 1
        isSubmitting = false
        statusSOS = ""
        noteText = ""
        recordedFile?.delete()
        recordedFile = null
    }

    fun nextStep() { _currentStep.value += 1 }
    fun prevStep() { if (_currentStep.value > 1) _currentStep.value -= 1 }

    // =========================
    // UTIL
    // =========================

    private suspend fun fileToBase64(file: File): String =
        withContext(Dispatchers.IO) {
            android.util.Base64.encodeToString(file.readBytes(), android.util.Base64.NO_WRAP)
        }

    private fun getToken(onResult: (String?) -> Unit) {
        FirebaseAuth.getInstance().currentUser
            ?.getIdToken(false)
            ?.addOnSuccessListener { onResult(it.token) }
            ?.addOnFailureListener { onResult(null) }
    }

    // =========================
    // MAIN FUNCTION
    // =========================

    fun guiSOS(
        context: Context,
        lat: Double,
        lng: Double,
        imageBitmap: android.graphics.Bitmap,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {

            try {
                isSubmitting = true
                statusSOS = "Đang xử lý dữ liệu..."

                // =========================
                // 1. BUILD REQUEST (IO)
                // =========================
                val request = withContext(Dispatchers.IO) {

                    var diaChi = "Tọa độ: $lat, $lng"

                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(lat, lng, 1)
                        if (!addresses.isNullOrEmpty()) {
                            diaChi = addresses[0].getAddressLine(0)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    val output = java.io.ByteArrayOutputStream()
                    imageBitmap.compress(
                        android.graphics.Bitmap.CompressFormat.JPEG,
                        60,
                        output
                    )

                    val base64Image =
                        android.util.Base64.encodeToString(output.toByteArray(), android.util.Base64.NO_WRAP)

                    val base64Audio = recordedFile?.let {
                        if (it.exists()) fileToBase64(it) else null
                    }

                    TinHieuSOSRequestDTO(
                        viDo = lat,
                        kinhDo = lng,
                        ghiAmBase64 = base64Audio,
                        hinhAnhBase64 = base64Image,
                        ghiChu = noteText,
                    )
                }

                // =========================
                // 2. GET TOKEN
                // =========================
                statusSOS = "Đang xác thực..."

                getToken { token ->

                    if (token == null) {
                        statusSOS = "Lỗi xác thực"
                        isSubmitting = false
                        onComplete(false)
                        return@getToken
                    }

                    // =========================
                    // 3. CALL API
                    // =========================
                    viewModelScope.launch {

                        statusSOS = "Đang gửi lên máy chủ..."

                        val response = withContext(Dispatchers.IO) {
                            BaoCaoSuCoRetrofit.api.submitSOS(
                                "Bearer $token",
                                request
                            )
                        }

                        if (response.isSuccessful) {
                            statusSOS = "Gửi thành công!"
                            onComplete(true)
                        } else {
                            statusSOS = "Lỗi hệ thống: ${response.code()}"
                            onComplete(false)
                        }

                        isSubmitting = false
                    }
                }

            } catch (e: Exception) {
                statusSOS = "Lỗi kết nối: ${e.localizedMessage}"
                isSubmitting = false
                onComplete(false)
            }
        }
    }
}
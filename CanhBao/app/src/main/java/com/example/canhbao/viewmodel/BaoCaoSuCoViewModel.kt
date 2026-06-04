package com.example.canhbao.viewmodel

import android.graphics.Bitmap
import android.util.Base64
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canhbao.data.model.suco.baocao.BaoCaoSuCoRequest
import com.example.canhbao.data.model.suco.loai.LoaiSuCo
import com.example.canhbao.data.network.BaoCaoSuCoRetrofit
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class BaoCaoSuCoViewModel : ViewModel() {

    var listLoaiSuCo by mutableStateOf(listOf<LoaiSuCo>())
    var selectedLoaiSuCo by mutableStateOf<LoaiSuCo?>(null)
    var isSubmitting by mutableStateOf(false)
    var uploadStatus by mutableStateOf("")

    private val auth = FirebaseAuth.getInstance()



    init {
        fetchLoaiSuCo()
    }

    fun fetchLoaiSuCo() {
        viewModelScope.launch {
            try {
                listLoaiSuCo = BaoCaoSuCoRetrofit.api.getAllLoaiSuCo()
                println("🔥 listLoaiSuCo = $listLoaiSuCo") // <-- Thêm dòng log ở đây
                selectedLoaiSuCo = listLoaiSuCo.firstOrNull()
            } catch (e: Exception) {
                uploadStatus = "Lỗi tải loại sự cố: ${e.message}"
                e.printStackTrace()
            }
        }
    }


    fun onLoaiSuCoSelected(loai: LoaiSuCo) {
        selectedLoaiSuCo = loai
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, output)
        return Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
    }

    fun guiBaoCao(
        bitmap: Bitmap,
        moTa: String,
        lat: Double,
        lng: Double
    ) {
        val user = auth.currentUser ?: run {
            uploadStatus = "Bạn chưa đăng nhập"
            return
        }

        viewModelScope.launch {
            try {
                isSubmitting = true
                uploadStatus = "Đang gửi báo cáo..."


                val base64Image = withContext(Dispatchers.IO) {
                    bitmapToBase64(bitmap) // chỉ Base64, bỏ "data:image/jpeg;base64,"
                }


                val request = BaoCaoSuCoRequest(
                    loaiSuCoId = selectedLoaiSuCo!!.id,
                    kinhDo = lng,
                    viDo = lat,
                    moTa = moTa,
                    hinhAnhUrl = base64Image
                )
                val token = withContext(Dispatchers.IO) {
                    user.getIdToken(false).result?.token
                } ?: ""

                val response = BaoCaoSuCoRetrofit.api.submitReport(
                    "Bearer $token",
                    request
                )

                val rawJson = response.body()?.toString()
                    ?: response.errorBody()?.string()

                if (response.isSuccessful) {
                    val ai = response.body()
                    if (ai != null) {
                        uploadStatus = when (ai.code) {
                            "AI_APPROVED" -> "AI_APPROVED: Duyệt (${ai.confidence}%)" // Thêm prefix AI_APPROVED
                            "AI_REJECTED" -> "AI_REJECTED: ${ai.message} (${ai.confidence}%)"
                            else -> ai.message
                        }
                    }
                }





            } catch (e: Exception) {
                uploadStatus = "Lỗi mạng: ${e.message}"
            } finally {
                isSubmitting = false
            }
        }

    }
}

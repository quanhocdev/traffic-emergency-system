package com.example.canhbao.viewmodel.tienich

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canhbao.data.model.suco.baocao.SuCoUserDto
import com.example.canhbao.data.model.qua.QuaResponseDTO
import com.example.canhbao.data.model.qua.doiqua.DoiQuaRequestDTO
import com.example.canhbao.data.model.qua.doiqua.TuiQuaResponseDTO
import com.example.canhbao.data.network.BaoCaoSuCoApi
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class QuaViewModel(private val api: BaoCaoSuCoApi) : ViewModel() {

    var allQua by mutableStateOf<List<QuaResponseDTO>>(emptyList())
    var filteredQua by mutableStateOf<List<QuaResponseDTO>>(emptyList())
    var userProfile by mutableStateOf<SuCoUserDto?>(null)
    var isLoading by mutableStateOf(false)
    var selectedTab by mutableStateOf("TẤT CẢ")
    private val auth = FirebaseAuth.getInstance()
    var listTuiQua by mutableStateOf<List<TuiQuaResponseDTO>>(emptyList())
    var isTuiLoading by mutableStateOf(false)

    var isExchanging by mutableStateOf(false)

    // ================== LOAD DATA ==================
    fun loadData() {
        viewModelScope.launch {
            try {

                val user = auth.currentUser ?: return@launch
                val token = user.getIdToken(false).await().token ?: return@launch

                userProfile = api.getUserInfo("Bearer $token")

                allQua = api.getAllQua()
                filterQua(selectedTab)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun thucHienDoiQua(
        qua: QuaResponseDTO,
        onResult: (Boolean, String) -> Unit
    ) {
        if (isExchanging) return

        viewModelScope.launch {
            isExchanging = true
            try {
                val user = auth.currentUser ?: return@launch
                val token = user.getIdToken(false).await().token ?: return@launch

                val dto = DoiQuaRequestDTO(
                    quaId = qua.id,
                    soLuong = 1
                )

                val response = api.exchangeQua("Bearer $token", dto)

                if (response.isSuccessful) {
                    loadData()
                    onResult(true, "Đổi quà thành công!")
                } else {
                    onResult(false, "Đổi quà thất bại: ${response.message()}")
                }

            } catch (e: Exception) {
                onResult(false, "Lỗi kết nối: ${e.message}")
            } finally {
                isExchanging = false
            }
        }
    }

    fun filterQua(loai: String) {
        selectedTab = loai
        filteredQua = if (loai == "TẤT CẢ") {
            allQua
        } else {
            allQua.filter { it.loai == loai }
        }
    }

    fun loadTuiQua() {
        viewModelScope.launch {
            isTuiLoading = true
            try {
                val user = FirebaseAuth.getInstance().currentUser ?: return@launch
                val token = user.getIdToken(false).await().token ?: return@launch

                listTuiQua = api.getMyGifts("Bearer $token")

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isTuiLoading = false
            }
        }
    }
}
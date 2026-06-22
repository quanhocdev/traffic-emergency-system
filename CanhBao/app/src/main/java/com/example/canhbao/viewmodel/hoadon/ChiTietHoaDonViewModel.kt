package com.example.canhbao.viewmodel.hoadon

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canhbao.data.model.hoadon.HoaDonUserResponseDTO
import com.example.canhbao.data.network.BaoCaoSuCoRetrofit
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChiTietHoaDonViewModel : ViewModel() {

    var hoaDon by mutableStateOf<HoaDonUserResponseDTO?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private suspend fun getToken(): String {

        val user =
            FirebaseAuth.getInstance()
                .currentUser
                ?: throw Exception("Chưa đăng nhập")

        val tokenResult =
            Tasks.await(
                user.getIdToken(false)
            )

        return "Bearer ${tokenResult.token}"
    }

    fun loadHoaDonDetail(
        hoaDonId: Long
    ) {

        viewModelScope.launch {

            try {

                Log.d(
                    "ChiTietHoaDon",
                    "STEP 1 - Start loadHoaDonDetail"
                )

                isLoading = true
                errorMessage = null

                val token =
                    withContext(Dispatchers.IO) {

                        Log.d(
                            "ChiTietHoaDon",
                            "STEP 2 - Getting token"
                        )

                        getToken()
                    }

                Log.d(
                    "ChiTietHoaDon",
                    "STEP 3 - Got token"
                )

                hoaDon =
                    withContext(Dispatchers.IO) {

                        Log.d(
                            "ChiTietHoaDon",
                            "STEP 4 - Calling getHoaDonDetail"
                        )

                        BaoCaoSuCoRetrofit.api.getHoaDonDetail(
                            token,
                            hoaDonId
                        )
                    }

                Log.d(
                    "ChiTietHoaDon",
                    "STEP 5 - Loaded hoaDon"
                )

                Log.d(
                    "ChiTietHoaDon",
                    "STEP 7 - Finished"
                )

            } catch (e: Exception) {

                Log.e(
                    "ChiTietHoaDon",
                    "MAIN ERROR",
                    e
                )

                errorMessage =
                    e.message ?: "Lỗi tải dữ liệu"

            } finally {

                isLoading = false

                Log.d(
                    "ChiTietHoaDon",
                    "STEP 8 - End"
                )
            }
        }
    }
}
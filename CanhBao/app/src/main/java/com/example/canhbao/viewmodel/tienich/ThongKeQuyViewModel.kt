package com.example.canhbao.viewmodel.tienich

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canhbao.data.model.tien.vinhdanh.ThongKeQuyRequestDTO
import com.example.canhbao.data.model.tien.vinhdanh.ThongKeQuyResponseDTO
import com.example.canhbao.data.network.BaoCaoSuCoRetrofit
import com.example.canhbao.data.network.PublicSocketManager
import com.example.canhbao.data.network.SocketClientProvider
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class FilterMode {
    DAY,
    MONTH,
    YEAR
}

class ThongKeQuyViewModel : ViewModel() {

    var thongKe by mutableStateOf(ThongKeQuyResponseDTO())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var filterMode by mutableStateOf(FilterMode.DAY)

    var selectedDate by mutableStateOf(LocalDate.now())

    private var publicSocketManager: PublicSocketManager? = null

    private val gson = Gson()

    fun init() {
        fetchThongKe()
        connectSocket()
    }

    fun nextPeriod() {

        selectedDate = when (filterMode) {
            FilterMode.DAY -> selectedDate.plusDays(1)
            FilterMode.MONTH -> selectedDate.plusMonths(1)
            FilterMode.YEAR -> selectedDate.plusYears(1)
        }

        fetchThongKe()
    }

    fun prevPeriod() {

        selectedDate = when (filterMode) {
            FilterMode.DAY -> selectedDate.minusDays(1)
            FilterMode.MONTH -> selectedDate.minusMonths(1)
            FilterMode.YEAR -> selectedDate.minusYears(1)
        }

        fetchThongKe()
    }

    fun fetchThongKe() {

        viewModelScope.launch {

            isLoading = true

            try {

                val request = when (filterMode) {

                    FilterMode.DAY ->

                        ThongKeQuyRequestDTO(
                            tuNgay = selectedDate.toString(),
                            denNgay = selectedDate.toString()
                        )

                    FilterMode.MONTH -> {

                        val first = selectedDate.withDayOfMonth(1)
                        val last = first.plusMonths(1).minusDays(1)

                        ThongKeQuyRequestDTO(
                            tuNgay = first.toString(),
                            denNgay = last.toString()
                        )
                    }

                    FilterMode.YEAR -> {

                        val first = selectedDate.withDayOfYear(1)
                        val last = first.plusYears(1).minusDays(1)

                        ThongKeQuyRequestDTO(
                            tuNgay = first.toString(),
                            denNgay = last.toString()
                        )
                    }
                }

                thongKe =
                    BaoCaoSuCoRetrofit.api.getThongKeQuy(request)

            } catch (e: Exception) {

                e.printStackTrace()

            } finally {

                isLoading = false

            }

        }

    }

    private fun connectSocket() {

        viewModelScope.launch {

            val client =
                SocketClientProvider.ensureConnected()

            if (publicSocketManager != null)
                return@launch

            publicSocketManager =
                PublicSocketManager(client).apply {

                    subscribe(object : PublicSocketManager.Callback {

                        override fun onPublicFundUpdate(json: String) {

                            try {

                                thongKe =
                                    gson.fromJson(
                                        json,
                                        ThongKeQuyResponseDTO::class.java
                                    )

                                Log.d(
                                    "ThongKeQuyVM",
                                    "Public fund updated"
                                )

                            } catch (e: Exception) {

                                e.printStackTrace()

                            }

                        }

                        override fun onSuCoUpdate(json: String) {}

                        override fun onSuCoDelete(id: Long) {}

                        override fun onTruSoUpdate(json: String) {}

                        override fun onTruSoDelete(id: Long) {}

                        override fun onCameraUpdate(json: String) {}

                        override fun onCameraDelete(id: Long) {}

                    })

                }

        }

    }

    override fun onCleared() {

        super.onCleared()

        publicSocketManager = null

    }

}
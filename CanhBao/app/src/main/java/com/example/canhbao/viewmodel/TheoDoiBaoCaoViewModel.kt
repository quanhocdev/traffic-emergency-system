package com.example.canhbao.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canhbao.data.model.suco.baocao.TheoDoiBaoCaoResponseDTO
import com.example.canhbao.data.network.AppConfig
import com.example.canhbao.data.network.BaoCaoSuCoRetrofit.api
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient

sealed class TheoDoiBaoCaoUiState {
    object Loading : TheoDoiBaoCaoUiState()
    data class Success(
        val data: List<TheoDoiBaoCaoResponseDTO>
    ) : TheoDoiBaoCaoUiState()

    data class Error(
        val message: String
    ) : TheoDoiBaoCaoUiState()
}

class TheoDoiBaoCaoViewModel : ViewModel() {

    var uiState: TheoDoiBaoCaoUiState by mutableStateOf(
        TheoDoiBaoCaoUiState.Loading
    )
        private set

    private var mStompClient: StompClient? = null

    private suspend fun getToken(): String {
        val user = FirebaseAuth.getInstance().currentUser
            ?: throw Exception("Chưa đăng nhập")

        val tokenResult = Tasks.await(
            user.getIdToken(false)
        )

        return "Bearer ${tokenResult.token ?: ""}"
    }

    fun fetchData() {
        loadDataFromApi()
        connectWebSocket()
    }

    private fun loadDataFromApi() {
        viewModelScope.launch {
            try {

                uiState = TheoDoiBaoCaoUiState.Loading

                val response = withContext(Dispatchers.IO) {
                    val token = getToken()
                    api.getTheoDoiSuCo(token)
                }

                uiState =
                    TheoDoiBaoCaoUiState.Success(response)

            } catch (e: Exception) {

                uiState =
                    TheoDoiBaoCaoUiState.Error(
                        e.localizedMessage
                            ?: "Lỗi kết nối Server"
                    )

                Log.e(
                    "TheoDoiBaoCao",
                    "loadData error: ${e.message}"
                )
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun connectWebSocket() {

        if (mStompClient?.isConnected == true)
            return

        mStompClient = Stomp.over(
            Stomp.ConnectionProvider.OKHTTP,
            "${AppConfig.WS_BASE_URL}/ws-suco/websocket"
        )

        listOf(
            "/topic/su-co",
            "/topic/user/history"
        ).forEach { topic ->

            mStompClient
                ?.topic(topic)
                ?.subscribe({

                    loadDataFromApi()

                }, {

                    Log.e(
                        "WebSocket",
                        "Error: ${it.message}"
                    )
                })
        }

        mStompClient?.connect()
    }

    fun cancelBaoCao(
        baoCaoId: Long
    ) {

        viewModelScope.launch {

            try {

                val success =
                    withContext(Dispatchers.IO) {

                        val token = getToken()

                        api.cancelSuCo(
                            token,
                            baoCaoId
                        ).isSuccessful
                    }

                if (success) {
                    loadDataFromApi()
                }

            } catch (e: Exception) {

                Log.e(
                    "TheoDoiBaoCao",
                    "cancel error: ${e.message}"
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mStompClient?.disconnect()
    }
}
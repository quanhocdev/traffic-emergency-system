package com.example.canhbao

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.canhbao.navigation.NavGraph
import com.example.canhbao.data.network.SocketClientProvider
import com.example.canhbao.viewmodel.call.CallViewModel
import com.example.canhbao.viewmodel.call.WebRTCViewModel
import com.example.canhbao.viewmodel.xacthuc.AuthViewModel
import com.mapbox.common.MapboxOptions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import ua.naiksoftware.stomp.dto.LifecycleEvent

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val callViewModel: CallViewModel by viewModels()
    private val webrtcViewModel: WebRTCViewModel by viewModels()

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        io.reactivex.plugins.RxJavaPlugins.setErrorHandler { throwable ->
            Log.e("STOMP_SAFE", "Bắt được lỗi ngầm: ${throwable.message}")
        }

        MapboxOptions.accessToken = BuildConfig.MAPBOX_PUBLIC_TOKEN

        dangKyHomThuCuocGoi()

        setContent {
            // Đảm bảo truyền webrtcViewModel xuống NavGraph để dùng chung trạng thái callState
            NavGraph(
                authViewModel = authViewModel,
                webrtcViewModel = webrtcViewModel
            )
        }
    }

    private fun dangKyHomThuCuocGoi() {
        lifecycleScope.launch {

            val sharedStompClient =
                SocketClientProvider.ensureConnected()

            val disposable =
                sharedStompClient.lifecycle()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ lifecycleEvent ->

                        when (lifecycleEvent.type) {

                            LifecycleEvent.Type.OPENED -> {
                                callViewModel.start(
                                    applicationContext,
                                    webrtcViewModel
                                )
                            }

                            else -> {}
                        }

                    }, {

                        Log.e(
                            "WebRTC_Debug",
                            "Lỗi vòng đời Socket",
                            it
                        )
                    })

            compositeDisposable.add(disposable)

            if (sharedStompClient.isConnected) {
                callViewModel.start(
                    applicationContext,
                    webrtcViewModel
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
        // Tuyệt đối KHÔNG disconnect stompClient ở đây vì nó là Singleton dùng chung cho cả Mapbox/Sự cố
    }
}
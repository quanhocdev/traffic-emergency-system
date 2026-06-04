package com.example.canhbao.viewmodel

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.example.canhbao.data.model.CameraMapDto
import com.example.canhbao.data.model.SuCoMapDto
import com.example.canhbao.data.model.TruSoMapDto
import com.example.canhbao.data.network.AppConfig
import com.example.canhbao.ui.screens.createCameraIcon
import org.json.JSONObject
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient

class RealtimeSocketManager(
    private val context: Context,
    private val stompClient: StompClient
) {

    interface Callback {
        fun onSuCoUpdate(suCo: SuCoMapDto)
        fun onSuCoRemove(id: Long)

        fun onTruSoRemove(id: Long)

        fun onCameraUpdate(camera: CameraMapDto)
        fun onCameraRemove(id: Long)
    }

    fun subscribe(callback: Callback) {

        // ================= SU CO =================
        stompClient.topic("/topic/su-co").subscribe({ msg ->
            try {
                val json = JSONObject(msg.payload)

                val id = json.optLong("id")
                val status = json.optString("trangThaiXuLy", "")

                if (status == "HOAN_THANH") {
                    callback.onSuCoRemove(id)
                    return@subscribe
                }

                val suCo = SuCoMapDto(
                    id = id,
                    kinhDo = json.optDouble("kinhDo"),
                    viDo = json.optDouble("viDo"),
                    iconUrl = json.optString("iconUrl"),
                    mucDoNghiemTrong = json.optString("mucDoNghiemTrong"),
                    moTa = json.optString("moTa"),
                    trangThaiDuyet = json.optString("trangThaiDuyet"),
                    trangThaiXuLy = status,
                    hinhAnhUrl = json.optString("hinhAnhUrl", null)
                )

                callback.onSuCoUpdate(suCo)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, { it.printStackTrace() })

        // ================= TRU SO DELETE =================
        stompClient.topic("/topic/tru-so-delete").subscribe({ msg ->
            val id = msg.payload.toLongOrNull()
            if (id != null) callback.onTruSoRemove(id)
        }, { it.printStackTrace() })

        // ================= CAMERA =================
        stompClient.topic("/topic/camera").subscribe({ msg ->
            try {
                val json = JSONObject(msg.payload)

                val camera = CameraMapDto(
                    id = json.getLong("id"),
                    tenCamera = json.getString("tenCamera"),
                    kinhDo = json.getDouble("kinhDo"),
                    viDo = json.getDouble("viDo"),
                    moTa = json.optString("moTa", null),
                    anhCamera = json.optString("anhCamera", null),
                    videoUrl = json.optString("videoUrl", null)
                )

                callback.onCameraUpdate(camera)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, { it.printStackTrace() })

        // ================= CAMERA DELETE =================
        stompClient.topic("/topic/camera-delete").subscribe({ msg ->
            val id = msg.payload.toLongOrNull()
            if (id != null) callback.onCameraRemove(id)
        }, { it.printStackTrace() })

        Log.d("RealtimeSocket", "Subscribed all topics")
    }
}
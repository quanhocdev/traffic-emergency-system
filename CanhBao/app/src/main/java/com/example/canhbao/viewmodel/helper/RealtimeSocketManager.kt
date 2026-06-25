package com.example.canhbao.viewmodel.helper

import android.content.Context
import android.util.Log
import com.example.canhbao.data.model.info.camera.CameraMapDto
import com.example.canhbao.data.model.info.truso.TruSoMapDto
import com.example.canhbao.data.model.suco.baocao.SuCoMapResponseDTO
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import ua.naiksoftware.stomp.StompClient

class RealtimeSocketManager(
    private val context: Context,
    private val stompClient: StompClient
) {

    interface Callback {
        fun onSuCoUpdate(suCo: SuCoMapResponseDTO)
        fun onSuCoRemove(id: Long)

        fun onTruSoUpdate(truSo: TruSoMapDto)
        fun onTruSoRemove(id: Long)
        fun onCameraUpdate(camera: CameraMapDto)
        fun onCameraRemove(id: Long)
    }

    fun subscribe(callback: Callback) {

        stompClient.topic("/topic/su-co")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()) // Đưa về Main Thread trước khi đẩy qua Callback UI
            .subscribe({ msg ->
                try {
                    val json = JSONObject(msg.payload)
                    val id = json.optLong("id")
                    val status = json.optString("trangThaiXuLy", "")

                    if (status == "HOAN_THANH" || status == "HUY_BO") {
                        callback.onSuCoRemove(id)
                        return@subscribe
                    }

                    // Lấy đúng trường dữ liệu mức độ từ Backend gửi về
                    val mucDoRaw = if (json.has("mucDoSuCo")) json.optString("mucDoSuCo") else json.optString("mucDoNghiemTrong", "NONE")

                    val suCo = SuCoMapResponseDTO(
                        id = id,
                        kinhDo = json.optDouble("kinhDo"),
                        viDo = json.optDouble("viDo"),
                        iconUrl = json.optString("iconUrl"),
                        mucDoSuCo = mucDoRaw, // Gán chuẩn biến tracking mức độ
                        trangThaiXuLy = status,
                        truSoId = if (json.has("truSoId")) json.optLong("truSoId") else null
                    )

                    callback.onSuCoUpdate(suCo)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, { it.printStackTrace() })


        // Trụ sở được tạo
        stompClient.topic("/topic/tru-so")  // đăng kí kênh
            .subscribeOn(Schedulers.io())                   // lắng nghe ở luồng I/O, xử lý mạng, đọc file
            .observeOn(AndroidSchedulers.mainThread())     // nhận tin thì chuyển về luồng chính, vẽ lên giao diện
            .subscribe({ msg -> // bắt đầu nhận tin với msg là dữ liệu thô từ server
                try {
                    // chuyển chuỗi văn bản nhận được thành đối tượng JSON để dễ bóc tách dữ liệu
                    val json = JSONObject(msg.payload)

                    // Khởi tạo đối tượng TruSoMapDto bằng cách bóc tách các trường dữ liệu tương ứng từ JSON
                    val truSo = TruSoMapDto(
                        id = json.getLong("id"),
                        tenTruSo = json.getString("tenTruSo"),
                        kinhDo = json.getDouble("kinhDo"),
                        viDo = json.getDouble("viDo"),
                        diaChi = json.optString("diaChi", "")
                    )

                    // Gửi đối tượng trụ sở qua hàm callback để UI hứng lấy và vẽ Marker lên bản đồ
                    callback.onTruSoUpdate(truSo)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, {
                it.printStackTrace()
            })

        // Trụ sở bị xóa
        stompClient.topic("/topic/tru-so-delete")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ msg ->
                val id = msg.payload.toLongOrNull()
                if (id != null) callback.onTruSoRemove(id)
            }, { it.printStackTrace() })

        // Camera được tạo
        stompClient.topic("/topic/camera")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ msg ->
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

        // Camera bị xóa
        stompClient.topic("/topic/camera-delete")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ msg ->
                val id = msg.payload.toLongOrNull()
                if (id != null) callback.onCameraRemove(id)
            }, { it.printStackTrace() })

        Log.d("RealtimeSocket", "Subscribed all topics safely on Main Thread")
    }
}
package com.example.canhbao.viewmodel.call

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.canhbao.data.model.call.CallSignalDto
import com.example.canhbao.data.network.BaoCaoSuCoRetrofit
import com.example.canhbao.data.network.SocketClientProvider
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.audio.JavaAudioDeviceModule

class WebRTCViewModel(application: Application) : AndroidViewModel(application) {

    private val gson = Gson()
    private val _callState = MutableStateFlow("IDLE")
    val callState = _callState.asStateFlow()

    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var audioSource: AudioSource? = null
    private val pendingCandidates = mutableListOf<IceCandidate>()
    private var targetUserId: String? = null

    private val iceServers = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer()
    )

    private var localAudioTrack: AudioTrack? = null

    private val _callerName = MutableStateFlow("Trụ sở công an")
    val callerName = _callerName.asStateFlow()

    init {
        initWebRTC()
    }

    private fun initWebRTC() {
        val options = PeerConnectionFactory.InitializationOptions.builder(getApplication())
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)

        // Tạo Module quản lý âm thanh phần cứng (Khử nhiễu, chống vang cho thiết bị)
        val audioDeviceModule = JavaAudioDeviceModule.builder(getApplication())
            .setUseHardwareAcousticEchoCanceler(true)
            .setUseHardwareNoiseSuppressor(true)
            .createAudioDeviceModule()

        val factoryOptions = PeerConnectionFactory.Options()
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(factoryOptions)
            .setAudioDeviceModule(audioDeviceModule)
            .createPeerConnectionFactory()

        val audioConstraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("googEchoCancellation", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googNoiseSuppression", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googAutoGainControl", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googHighpassFilter", "true"))
        }

        audioSource = peerConnectionFactory?.createAudioSource(audioConstraints)
        localAudioTrack = peerConnectionFactory?.createAudioTrack("101", audioSource)
        localAudioTrack?.setEnabled(true)

        Log.d("WebRTC", "Khởi tạo thành công với Hardware AEC")
    }

    // --- 1. CHỦ ĐỘNG GỌI (Sử dụng client tập trung từ Provider) ---
    fun startCall(targetId: String, myId: String) {
        setupAudioConfig()
        _callState.value = "CALLING"

        if (peerConnection == null) {
            peerConnection = createPeerConnection(targetId, myId)
        }

        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        }

        peerConnection?.createOffer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                peerConnection?.setLocalDescription(SimpleSdpObserver(), sdp)

                val signal = CallSignalDto(
                    type = "offer",
                    to = targetId,
                    from = myId,
                    sdp = sdp?.description
                )
                sendSignalSafely(signal)
            }
        }, constraints)
    }

    // --- 2. XỬ LÝ TÍN HIỆU TỪ SOCKET ---
    fun handleSignal(json: JSONObject, myId: String) {
        val signal = gson.fromJson(json.toString(), CallSignalDto::class.java)
        val fromUser = signal.from ?: ""

        when (signal.type?.uppercase()) {
            "BYE" -> {
                Log.w("WebRTC", "Trụ sở đã cúp máy")
                peerConnection?.close()
                peerConnection = null
                _callState.value = "IDLE"
                val audioManager = getApplication<Application>().getSystemService(Context.AUDIO_SERVICE) as AudioManager
                audioManager.mode = AudioManager.MODE_NORMAL
            }
            "OFFER" -> {
                targetUserId = fromUser
                setupAudioConfig()
                fetchAndSetTruSoName(fromUser)

                _callState.value = "INCOMING"
                val sdp = SessionDescription(SessionDescription.Type.OFFER, signal.sdp)
                if (peerConnection == null) {
                    peerConnection = createPeerConnection(fromUser, myId)
                }

                peerConnection?.setRemoteDescription(object : SimpleSdpObserver() {
                    override fun onSetSuccess() {
                        Log.d("WebRTC", "Đã nhận OFFER thành công, đang chờ nhấc máy...")
                        pendingCandidates.forEach { peerConnection?.addIceCandidate(it) }
                        pendingCandidates.clear()
                    }
                }, sdp)
            }
            "ANSWER" -> {
                val sdp = SessionDescription(SessionDescription.Type.ANSWER, signal.sdp)
                peerConnection?.setRemoteDescription(SimpleSdpObserver(), sdp)
            }
            "CANDIDATE" -> {
                val candidateData = signal.candidate as? Map<*, *>
                candidateData?.let {
                    val sdpCandidate = it["candidate"] as? String ?: ""
                    val sdpMid = it["sdpMid"] as? String ?: ""
                    val sdpMLineIndex = (it["sdpMLineIndex"] as? Number)?.toInt() ?: 0

                    val candidate = IceCandidate(sdpMid, sdpMLineIndex, sdpCandidate)

                    if (peerConnection?.remoteDescription != null) {
                        peerConnection?.addIceCandidate(candidate)
                        Log.d("WebRTC", "✅ Đã thêm Candidate trực tiếp")
                    } else {
                        pendingCandidates.add(candidate)
                        Log.d("WebRTC", "⏳ Đã lưu candidate tạm thời vào hàng đợi")
                    }
                }
            }
        }
    }

    // --- 3. HÀM CHẤP NHẬN CUỘC GỌI ---
    fun acceptCall(myId: String) {
        val currentTargetId = targetUserId
        val activeClient = SocketClientProvider.stompClient

        if (!activeClient.isConnected || peerConnection == null || currentTargetId == null) {
            Log.e("WebRTC", "Không thể nhấc máy: Thiếu thông tin hoặc Socket mất kết nối")
            return
        }

        _callState.value = "CONNECTED"

        peerConnection?.createAnswer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                peerConnection?.setLocalDescription(SimpleSdpObserver(), sdp)

                val signal = CallSignalDto(
                    type = "answer",
                    to = currentTargetId,
                    from = myId,
                    sdp = sdp?.description
                )
                sendSignalSafely(signal)
            }
        }, MediaConstraints())
    }

    // --- 4. CẤU HÌNH KẾT NỐI PEER TO PEER ---
    private fun createPeerConnection(targetId: String, myId: String): PeerConnection? {
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        val pc = peerConnectionFactory?.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate?) {
                candidate?.let {
                    val iceMap = mapOf("sdpMid" to it.sdpMid, "sdpMLineIndex" to it.sdpMLineIndex, "candidate" to it.sdp)
                    val signal = CallSignalDto(
                        type = "candidate",
                        to = targetId,
                        from = myId,
                        candidate = iceMap
                    )
                    sendSignalSafely(signal)
                }
            }

            override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState?) {
                Log.d("WebRTC", "ICE Connection State thay đổi: $newState")
                if (newState == PeerConnection.IceConnectionState.CONNECTED) {
                    applySonyAudioFix()
                }
            }

            override fun onAddTrack(receiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) {
                val track = receiver?.track()
                if (track is AudioTrack) {
                    track.setEnabled(true)
                    track.setVolume(1.0)
                    Log.d("WebRTC", "Đã nhận luồng âm thanh đầu ra thành công (Volume: 1.0)")
                }
            }

            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
            override fun onRemoveStream(p0: MediaStream?) {}
            override fun onDataChannel(p0: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onAddStream(p0: MediaStream?) {}
        })

        localAudioTrack?.let { track ->
            pc?.addTrack(track, listOf("ARDMSv0"))
        }

        return pc
    }

    private fun applySonyAudioFix() {
        val audioManager = getApplication<Application>().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isSpeakerphoneOn = true
        audioManager.setStreamVolume(
            AudioManager.STREAM_VOICE_CALL,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
            0
        )
    }

    private fun setupAudioConfig() {
        val audioManager = getApplication<Application>().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isSpeakerphoneOn = true
    }

    fun toggleMic(isMuted: Boolean) {
        localAudioTrack?.setEnabled(!isMuted)
        Log.d("WebRTC", "Trạng thái Micro: ${if (isMuted) "ĐÃ TẮT" else "ĐANG MỞ"}")
    }

    fun endCall(myId: String? = null) {
        val currentTarget = targetUserId

        if (currentTarget != null && myId != null) {
            val signal = CallSignalDto(
                type = "BYE",
                to = currentTarget,
                from = myId
            )
            sendSignalSafely(signal)
        }

        try {
            peerConnection?.close()
        } catch (e: Exception) {
            Log.e("WebRTC", "Lỗi khi đóng kết nối PeerConnection: ${e.message}")
        }

        peerConnection = null
        targetUserId = null
        _callState.value = "IDLE"
        _callerName.value = "Trụ sở cứu hộ"

        val audioManager = getApplication<Application>().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_NORMAL
    }

    private fun sendSignalSafely(signal: CallSignalDto) {
        val activeClient = SocketClientProvider.stompClient
        if (activeClient.isConnected) {
            activeClient.send("/app/call-signal", gson.toJson(signal))
                .subscribe({
                    Log.d("WebRTC", "Gửi gói tin signal [${signal.type}] thành công")
                }, { throwable ->
                    Log.e("WebRTC", "Lỗi phát sinh khi đẩy gói tin tín hiệu: ${throwable.message}")
                })
        } else {
            Log.w("WebRTC", "Không thể gửi gói tín hiệu vì Socket trung tâm đã đóng")
        }
    }

    open class SimpleSdpObserver : SdpObserver {
        override fun onCreateSuccess(p0: SessionDescription?) {}
        override fun onSetSuccess() {}
        override fun onCreateFailure(p0: String?) { Log.e("WebRTC", "Lỗi khởi tạo SDP: $p0") }
        override fun onSetFailure(p0: String?) { Log.e("WebRTC", "Lỗi cấu hình thiết lập SDP: $p0") }
    }

    override fun onCleared() {
        endCall(null)
        audioSource?.dispose()
        peerConnectionFactory?.dispose()
        super.onCleared()
    }

    private fun fetchAndSetTruSoName(fromUser: String) {
        if (fromUser.startsWith("TRU_SO_")) {
            val truSoId = fromUser.substringAfter("TRU_SO_").toLongOrNull()
            if (truSoId != null) {
                viewModelScope.launch {
                    try {
                        val listTruSo = withContext(Dispatchers.IO) {
                            BaoCaoSuCoRetrofit.api.getAllTruSo()
                        }
                        val targetTruSo = listTruSo.find { it.id == truSoId }

                        if (targetTruSo != null && !targetTruSo.tenTruSo.isNullOrBlank()) {
                            _callerName.value = targetTruSo.tenTruSo
                        } else {
                            _callerName.value = "Trụ sở tiếp nhận số $truSoId"
                        }
                    } catch (e: Exception) {
                        Log.e("WebRTC_API", "Lỗi tra cứu thông tin tên trụ sở từ Backend: ${e.message}")
                        _callerName.value = "Trụ sở số $truSoId"
                    }
                }
            }
        } else {
            _callerName.value = fromUser
        }
    }
}
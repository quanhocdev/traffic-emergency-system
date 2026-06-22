package com.example.canhbao.data.model.call

data class CallSignalDto(
    val type: String,           // OFFER, ANSWER, hoặc CANDIDATE
    val from: String,           // ID người gửi (ví dụ: TRU_SO_1 hoặc UID người dùng)
    val to: String,             // ID người nhận
    val sdp: String? = null,    // Nội dung Session Description (chỉ có khi type là OFFER/ANSWER)
    val candidate: Any? = null  // Thông tin ICE Candidate (chỉ có khi type là CANDIDATE)
)
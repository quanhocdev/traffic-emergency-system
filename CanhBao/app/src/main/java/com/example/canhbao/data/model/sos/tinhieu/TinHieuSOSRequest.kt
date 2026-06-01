package com.example.canhbao.data.model.sos.tinhieu

data class TinHieuSOSRequest(
    val viDo: Double,
    val kinhDo: Double,
    val ghiAmBase64: String? = null,
    val hinhAnhBase64: String,
    val ghiChu: String? = null,
    val thoiGianTao: String,
    val diaChi: String? = null
)
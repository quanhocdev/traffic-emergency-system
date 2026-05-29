package com.example.canhbao.data.model.qua.doiqua

data class TuiQuaResponseDTO(
    val quaId: Long,
    val tenQua: String,
    val soLuong: Int,
    val loai: String,
    val ngayKetThuc: String?
)
package com.example.canhbao.data.model.qua

import java.math.BigDecimal

data class QuaResponseDTO(
    val id: Long,
    val ten: String,
    val loai: String,
    val moTa: String?,
    val diem: Int,
    val hinhAnh: String?,
    val giaTriGiamPercent: Int?,
    val giaTriToiDa: BigDecimal?,
    val ngayKetThuc: String?,
    val trangThai: String
)



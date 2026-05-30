package com.example.canhbao.data.model.hoadon

import java.math.BigDecimal
import java.time.LocalDateTime

data class HoaDonResponseDTO(
    val id: Long? = null,
    val sosId: Long? = null,
    val trusoId: Long? = null,
    val userId: String? = null,
    val noiDungXuLy: String? = null,
    val thanhTien: BigDecimal? = null,
    val createdAt: LocalDateTime? = null,
    val trangThai: String? = null
)
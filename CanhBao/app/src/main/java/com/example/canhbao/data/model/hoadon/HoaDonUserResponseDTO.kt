package com.example.canhbao.data.model.hoadon

import java.math.BigDecimal
import java.time.LocalDateTime

data class HoaDonUserResponseDTO(
    val id: Long? = null,
    val noiDungXuLy: String? = null,
    val thanhTien: BigDecimal? = null,
    val trangThai: String? = null,
    val createdAt: LocalDateTime? = null
)
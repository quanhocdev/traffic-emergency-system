package com.example.canhbao.data.model.hoadon

import com.example.canhbao.data.model.TruSoMiniDTO
import java.math.BigDecimal

data class UserMiniDTO(
    val name: String? = null,
    val email: String? = null,
)
data class HoaDonUserResponseDTO(
    val id: Long? = null,
    val truSo: TruSoMiniDTO? = null,
    val user: UserMiniDTO? = null,
    val noiDungXuLy: String? = null,
    val thanhTien: BigDecimal? = null,
    val trangThai: String? = null,
    val createdAt: String? = null
)
package com.example.canhbao.data.model.hoadon

import com.example.canhbao.data.model.sos.tinhieu.UserInfoResponseDTO
import com.example.canhbao.data.model.truso.TruSoMiniDTO
import java.math.BigDecimal
data class HoaDonUserResponseDTO(
    val id: Long? = null,
    val truSo: TruSoMiniDTO? = null,
    val user: UserInfoResponseDTO? = null,
    val noiDungXuLy: String? = null,
    val thanhTien: BigDecimal? = null,
    val trangThai: String? = null,
    val createdAt: String? = null
)
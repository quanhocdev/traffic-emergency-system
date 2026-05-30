package com.example.canhbao.data.model.hoadon.payment

import java.math.BigDecimal
import java.time.LocalDateTime

data class ThanhToanResponseDTO (
    val thanhToanId: Long? = null,

    val hoaDonId: Long? = null,

    val trusoId: Long? = null,

    val phuongThucThanhToan: String? = null,

    val maGiaoDich: String? = null,

    val trangThai: String? = null,

    val thanhTien: BigDecimal? = null,

    val soTienGiam: BigDecimal? = null,

    val tongThanhToan: BigDecimal? = null,

    val message: String? = null,

    val createdAt: String? = null
)
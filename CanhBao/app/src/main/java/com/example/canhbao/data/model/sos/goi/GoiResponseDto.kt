package com.example.canhbao.data.model.sos.goi
import java.math.BigDecimal

data class GoiResponseDto(
    val id: Long,
    val ten: String,
    val thoiHan: Int,
    val gia: BigDecimal,
    val khoangCachMienPhi: Int,
    val uuDai: String
)


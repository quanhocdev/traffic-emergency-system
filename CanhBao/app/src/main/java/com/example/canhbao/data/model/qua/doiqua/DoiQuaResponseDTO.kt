package com.example.canhbao.data.model.qua.doiqua

import com.example.canhbao.data.model.qua.QuaResponseDTO
import java.time.LocalDateTime

data class DoiQuaResponseDTO (
    val id: Long? = null,
    val qua: QuaResponseDTO? = null,
    val soLuong: Int? = null,
    val diemDaTru: Int? = null,
    val ngayDoi: LocalDateTime? = null
)
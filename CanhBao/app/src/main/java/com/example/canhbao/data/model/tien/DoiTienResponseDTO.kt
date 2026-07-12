package com.example.canhbao.data.model.tien

data class DoiTienResponseDTO(
    val id: Long = 0L,
    val soDiemDoi: Int = 0,
    val giaTri: Long = 0L,
    val loaiDoi: String = "",
    val ngayDoi: String = ""
)
package com.example.canhbao.data.model.tien

data class ThongKeQuyDto(
    val tongGiaTri: Long = 0L,
    val bangVinhDanh: List<VinhDanhDTO> = emptyList()
)
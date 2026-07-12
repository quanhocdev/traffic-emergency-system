package com.example.canhbao.data.model.tien.vinhdanh

import com.example.canhbao.data.model.tien.vinhdanh.VinhDanhDTO

data class ThongKeQuyResponseDTO(
    val tongGiaTri: Long = 0L,
    val bangVinhDanh: List<VinhDanhDTO> = emptyList()
)
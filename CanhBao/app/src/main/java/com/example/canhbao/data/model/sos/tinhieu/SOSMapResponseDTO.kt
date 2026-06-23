package com.example.canhbao.data.model.sos.tinhieu

import com.example.canhbao.data.model.info.truso.TruSoMiniDTO

data class SOSMapResponseDTO(
    val id: Long?,
    val viDo: Double?,
    val kinhDo: Double?,
    val trangThai: String,
    val truSo: TruSoMiniDTO? 
)
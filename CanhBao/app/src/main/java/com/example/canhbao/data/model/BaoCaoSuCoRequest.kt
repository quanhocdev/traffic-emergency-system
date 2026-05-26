package com.example.canhbao.data.model

data class LoaiSuCoIdRequest(val id: Long)

data class BaoCaoSuCoRequest(
    val loaiSuCoId: Long,
    val kinhDo: Double,
    val viDo: Double,
    val moTa: String,
    val hinhAnhUrl: String
)
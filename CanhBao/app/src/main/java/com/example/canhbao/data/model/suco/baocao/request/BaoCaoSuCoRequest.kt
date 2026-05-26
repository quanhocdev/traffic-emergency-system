package com.example.canhbao.data.model.suco.baocao.request

data class LoaiSuCoIdRequest(val id: Long)

data class BaoCaoSuCoRequest(
    val loaiSuCoId: Long,
    val kinhDo: Double,
    val viDo: Double,
    val moTa: String,
    val hinhAnhUrl: String
)
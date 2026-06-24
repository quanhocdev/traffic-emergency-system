package com.example.canhbao.data.model.sos.goi

data class MuaGoiDto(
    val id: Long,
    val userId: String,
    val goiId: Long,
    val tenGoi: String,
    val ngayMua: String,
    val ngayHetHan: String,
    val trangThai: String
)
package com.example.canhbao.data.model.sos.goi

data class MuaGoiUserResponseDto(
    val goi: GoiResponseDto,
    val ngayMua: String,
    val ngayHetHan: String,
    val trangThai: String
)
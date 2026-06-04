package com.example.canhbao.data.model.suco.baocao

data class UserSuCoDetailResponseDTO(
    val id: Long,
    val viDo: Double,
    val kinhDo: Double,
    val moTa: String?,
    val tenLoai: String?,
    val iconUrl: String?,
    val trangThaiDuyet: String?,
    val trangThaiXuLy: String?,
    val mucDoNghiemTrong: String?,
    val hinhAnhUrl: String?,
    val doTinCay: Int?,
    val tenNguoiBao: String?,
    val diaChi: String?,
    val thoiGianTao: String?
)
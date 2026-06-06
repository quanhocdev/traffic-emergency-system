package com.example.canhbao.data.model.sos.tinhieu

data class TheoDoiSOSDetailResponseDTO(
    val id: Long,
    val viDo: Double?,
    val kinhDo: Double?,
    val diaChi: String?,
    val ghiChu: String?,
    val hinhAnh: String?,
    val ghiAm: String?,
    val trangThai: String?,
    val createdAt: String?,
    val idTruSoTiepNhan: Long?,
    val tenTruSoTiepNhan: String?,
    val hoaDonId: Long?,
    val thanhTien: Double?,
    val trangThaiHoaDon: String?
)
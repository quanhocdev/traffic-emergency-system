package com.example.canhbao.data.model.suco.baocao

data class TheoDoiBaoCaoResponseDTO(
    val id: Long,
    val tenLoai: String?,
    val moTa: String?,
    val hinhAnhUrl: String?,
    val diaChi: String?,
    val trangThaiDuyet: String?,
    val trangThaiXuLy: String?,
    val doTinCay: Int?,
    val idTruSoTiepNhan: Long?,
    val tenTruSoTiepNhan: String?,
    val mucDoNghiemTrong: String?,
    val thoiGianTao: String?
)
package com.example.canhbao.data.model.suco.baocao

import com.example.canhbao.data.model.truso.TruSoMapDto
import com.example.canhbao.data.model.sos.tinhieu.UserInfoResponseDTO
data class TheoDoiSuCoDetailResponseDTO(
    val id: Long,
    val tenLoai: String?,
    val moTa: String?,
    val hinhAnhUrl: String?,
    val diaChi: String?,
    val trangThaiXuLy: String?,
    val doTinCay: Int?,
    val truSo: TruSoMapDto,
    val user: UserInfoResponseDTO,
    val mucDoNghiemTrong: String?,
    val thoiGianTao: String?
)
package com.example.canhbao.data.model.sos.tinhieu

import com.example.canhbao.data.model.TruSoMapDto;
import com.example.canhbao.data.model.sos.tinhieu.UserInfoResponseDTO;

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
    val truSo: TruSoMapDto,
    val user: UserInfoResponseDTO,
    val hoaDonId: Long?,
    val thanhTien: Double?,
    val trangThaiHoaDon: String?
)
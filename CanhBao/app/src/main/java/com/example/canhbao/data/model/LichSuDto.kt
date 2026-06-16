package com.example.canhbao.data.model

import com.example.canhbao.data.model.hoadon.HoaDonUserResponseDTO
import com.example.canhbao.data.model.hoadon.payment.ThanhToanResponseDTO


data class LichSuDto(
    val id: Long,
    val loai: String?,
    val tieuDe: String?,
    val moTa: String?,
    val trangThaiXuLy: String?,
    val hinhAnhUrl: String?,
    val viDo: Double?,
    val kinhDo: Double?,
    val ghiAmUrl: String?,
    val thongTinTiepNhan: String?,
    val thoiGian: String?,
    val diaChi: String?,
    val hoaDon: HoaDonUserResponseDTO?,
    val thanhToan: ThanhToanResponseDTO?
)
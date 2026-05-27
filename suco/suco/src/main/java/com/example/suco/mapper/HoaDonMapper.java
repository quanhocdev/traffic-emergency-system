package com.example.suco.mapper;
import com.example.suco.dto.sos.payment.hoadon.request.HoaDonRequestDTO;
import com.example.suco.dto.sos.payment.hoadon.response.HoaDonResponseDTO;
import com.example.suco.model.HoaDon;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;


@Component
public class HoaDonMapper {

        // Chuyển đổi từ DTO HoaDonRequestDTO sang entity HoaDon
    public HoaDon toEntity( HoaDonRequestDTO req, Long trusoId, String userId, BigDecimal gia ) {

        HoaDon hd = new HoaDon();

        hd.setSosId(req.getSosId());
        hd.setTrusoId(trusoId);
        hd.setUserId(userId);
        hd.setNoiDungXuLy(req.getNoiDungXuLy());

        hd.setThanhTien(gia);

        hd.setSoTienGiam(BigDecimal.ZERO);
        hd.setTongThanhToan(gia);

        hd.setTrangThai("PENDING");


        return hd;
    }

    // Chuyển đổi từ entity HoaDon sang DTO HoaDonResponseDTO
    public HoaDonResponseDTO toDTO(HoaDon hd) {

        HoaDonResponseDTO dto = new HoaDonResponseDTO();

        dto.setId(hd.getId());
        dto.setSosId(hd.getSosId());
        dto.setTrusoId(hd.getTrusoId());
        dto.setUserId(hd.getUserId());
        dto.setNoiDungXuLy(hd.getNoiDungXuLy());
        dto.setThanhTien(hd.getThanhTien());
        dto.setSoTienGiam(hd.getSoTienGiam());
        dto.setTongThanhToan(hd.getTongThanhToan());
        dto.setTrangThai(hd.getTrangThai());
        dto.setCreatedAt(hd.getCreatedAt());

        return dto;
    }
}
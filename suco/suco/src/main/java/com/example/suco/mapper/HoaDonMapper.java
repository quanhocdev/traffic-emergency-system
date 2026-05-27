package com.example.suco.mapper;
import com.example.suco.dto.sos.payment.hoadon.response.HoaDonResponseDTO;
import com.example.suco.model.HoaDon;
import org.springframework.stereotype.Component;


@Component
public class HoaDonMapper {

    public HoaDonResponseDTO toDTO(HoaDon hd) {

        HoaDonResponseDTO dto = new HoaDonResponseDTO();

        dto.setId(hd.getId());
        dto.setSosId(hd.getSosId());
        dto.setTrusoId(hd.getTrusoId());
        dto.setUserId(hd.getUserId());
        dto.setTenSos(hd.getTenSos());
        dto.setNoiDungXuLy(hd.getNoiDungXuLy());
        dto.setThanhTien(hd.getThanhTien());
        dto.setSoTienGiam(hd.getSoTienGiam());
        dto.setTongThanhToan(hd.getTongThanhToan());
        dto.setTrangThai(hd.getTrangThai());
        dto.setCreatedAt(hd.getCreatedAt());

        return dto;
    }
}
package com.example.suco.mapper;

import com.example.suco.dto.sos.hoadon.payment.ThanhToanRequestDTO;
import com.example.suco.dto.sos.hoadon.payment.ThanhToanResponseDTO;
import com.example.suco.model.HoaDon;
import com.example.suco.model.ThanhToanHoaDon;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ThanhToanCuuHoMapper {

    // Request -> Entity
    public ThanhToanHoaDon toEntity(
            HoaDon hd,
            ThanhToanRequestDTO request,
            BigDecimal thanhTien,
            BigDecimal soTienGiam,
            BigDecimal tongThanhToan
    ) {

        ThanhToanHoaDon thanhToan =
                new ThanhToanHoaDon();

        thanhToan.setHoaDon(hd);

        thanhToan.setPhuongThucThanhToan(
                request.getPhuongThucThanhToan()
        );

        thanhToan.setTrangThai("SUCCESS");

        thanhToan.setThanhTien(thanhTien);

        thanhToan.setQuaId(
                request.getQuaId()
        );

        thanhToan.setSoTienGiam(
                soTienGiam
        );

        thanhToan.setTongThanhToan(
                tongThanhToan
        );

        return thanhToan;
    }

    // Entity -> Response
    public ThanhToanResponseDTO toDTO(
            ThanhToanHoaDon thanhToan
    ) {

        ThanhToanResponseDTO response =
                new ThanhToanResponseDTO();

        response.setThanhToanId(
                thanhToan.getId()
        );

        response.setHoaDonId(
                thanhToan.getHoaDon().getId()
        );

        response.setTrusoId(
                thanhToan.getHoaDon().getTrusoId()
        );

        response.setPhuongThucThanhToan(
                thanhToan.getPhuongThucThanhToan()
        );

        response.setMaGiaoDich(
                thanhToan.getMaGiaoDich()
        );

        response.setTrangThai(
                thanhToan.getTrangThai()
        );

        response.setThanhTien(
                thanhToan.getThanhTien()
        );

        response.setSoTienGiam(
                thanhToan.getSoTienGiam()
        );

        response.setTongThanhToan(
                thanhToan.getTongThanhToan()
        );

        response.setCreatedAt(
                thanhToan.getCreatedAt()
        );

        response.setMessage(
                "Thanh toán thành công"
        );

        return response;
    }
}
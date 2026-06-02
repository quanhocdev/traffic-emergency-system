package com.example.suco.mapper;
import com.example.suco.dto.sos.hoadon.quanly.HoaDonRequestDTO;
import com.example.suco.dto.sos.hoadon.quanly.HoaDonResponseDTO;
import com.example.suco.dto.sos.hoadon.quanly.HoaDonUserResponseDTO;
import com.example.suco.dto.sos.hoadon.quanly.TruSoMiniDTO;
import com.example.suco.dto.sos.tinhieu.UserMiniDTO;
import com.example.suco.model.HoaDon;
import com.example.suco.model.TruSo;
import com.example.suco.repository.vanhanh.TruSoRepository;
import com.example.suco.repository.vanhanh.UserRepository;
import com.example.suco.model.User;
import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class HoaDonCuuHoMapper {
    



        // Chuyển đổi từ DTO HoaDonRequestDTO sang entity HoaDon
    public HoaDon toEntity( HoaDonRequestDTO req, Long trusoId, String userId, BigDecimal gia ) {

        HoaDon hd = new HoaDon();

        hd.setSosId(req.getSosId());
        hd.setTrusoId(trusoId);
        hd.setUserId(userId);
        hd.setNoiDungXuLy(req.getNoiDungXuLy());
        hd.setTrangThai("PENDING");
        hd.setThanhTien(gia);
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
        dto.setCreatedAt(hd.getCreatedAt());
        dto.setTrangThai(hd.getTrangThai());

        return dto;
    }

    // Chuyển đổi từ entity HoaDon sang DTO HoaDonUserResponseDTO
   public HoaDonUserResponseDTO toUserDTO(
        HoaDon hd,
        User userEntity,
        TruSo truSoEntity
) {

    HoaDonUserResponseDTO dto = new HoaDonUserResponseDTO();

    dto.setId(hd.getId());

    // TRỤ SỞ
    TruSoMiniDTO truSo = null;
    if (truSoEntity != null) {
        truSo = new TruSoMiniDTO();
        truSo.setId(truSoEntity.getId());
        truSo.setTenTruSo(truSoEntity.getTenTruSo());
        truSo.setDiaChi(truSoEntity.getDiaChi());
    }
    dto.setTruSo(truSo);

    // USER
    UserMiniDTO user = null;
    if (userEntity != null) {
        user = new UserMiniDTO();
        user.setName(userEntity.getName());
        user.setEmail(userEntity.getEmail());
    }
    dto.setUser(user);

    dto.setNoiDungXuLy(hd.getNoiDungXuLy());
    dto.setThanhTien(hd.getThanhTien());
    dto.setTrangThai(hd.getTrangThai());
    dto.setCreatedAt(hd.getCreatedAt());

    return dto;
}
}
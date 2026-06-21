package com.example.suco.mapper;

import com.example.suco.dto.sos.hoadon.quanly.HoaDonRequestDTO;
import com.example.suco.dto.sos.hoadon.quanly.HoaDonResponseDTO;
import com.example.suco.dto.sos.hoadon.quanly.HoaDonTruSoResponseDTO;
import com.example.suco.dto.sos.hoadon.quanly.HoaDonUserResponseDTO;
import com.example.suco.dto.sos.hoadon.quanly.TruSoMiniDTO;
import com.example.suco.dto.sos.tinhieu.UserInfoResponseDTO; 
import com.example.suco.dto.sos.tinhieu.UserMiniDTO; 
import com.example.suco.mapper.InfoUserMapper;
import com.example.suco.model.HoaDon;
import com.example.suco.model.TruSo;
import com.example.suco.repository.vanhanh.UserRepository;
import com.example.suco.service.sos.tinhieu.user.workflow.gui.VipService;
import com.example.suco.model.User;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HoaDonCuuHoMapper {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VipService vipService;

    @Autowired
    private InfoUserMapper infoUserMapper;

    @Autowired
    private InfoTruSoMapper infoTruSoMapper;

    // Chuyển đổi từ DTO HoaDonRequestDTO sang entity HoaDon
    public HoaDon toEntity(HoaDonRequestDTO req, Long trusoId, String userId, BigDecimal gia) {
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

    public HoaDonTruSoResponseDTO toTruSoDTO(HoaDon hd) {
        if (hd == null) return null;

        HoaDonTruSoResponseDTO dto = new HoaDonTruSoResponseDTO();
        dto.setId(hd.getId());
        dto.setNoiDungXuLy(hd.getNoiDungXuLy());
        dto.setThanhTien(hd.getThanhTien());
        dto.setTrangThai(hd.getTrangThai());
        dto.setCreatedAt(hd.getCreatedAt());

        if (hd.getUserId() != null) {
            User userEntity = userRepository.findByUid(hd.getUserId()).orElse(null);
            if (userEntity != null) {
                UserInfoResponseDTO userInfo = infoUserMapper.toUserInfoResponseDTO(userEntity);
                dto.setUser(userInfo);
            }
        }

        return dto;
    }

    // Chuyển đổi từ entity HoaDon sang DTO HoaDonUserResponseDTO (Dùng UserMiniDTO - Kế thừa)
    public HoaDonUserResponseDTO toUserDTO(HoaDon hd, User userEntity, TruSo truSoEntity) {
        HoaDonUserResponseDTO dto = new HoaDonUserResponseDTO();
        dto.setId(hd.getId());

        dto.setTruSo(infoTruSoMapper.toMiniDto(truSoEntity));
        dto.setUser(infoUserMapper.toUserMiniDTO(userEntity));

        dto.setNoiDungXuLy(hd.getNoiDungXuLy());
        dto.setThanhTien(hd.getThanhTien());
        dto.setTrangThai(hd.getTrangThai());
        dto.setCreatedAt(hd.getCreatedAt());

        return dto;
    }
}
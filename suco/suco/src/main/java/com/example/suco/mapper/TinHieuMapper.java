package com.example.suco.mapper;

import com.example.suco.dto.sos.hoadon.quanly.TruSoMiniDTO;
import com.example.suco.dto.sos.tinhieu.AdminSOSDetailResponseDTO;
import com.example.suco.dto.sos.tinhieu.SOSMapResponseDTO;
import com.example.suco.dto.sos.tinhieu.TheoDoiSOSDetailResponseDTO;
import com.example.suco.dto.sos.tinhieu.TheoDoiSOSItemResponseDTO;
import com.example.suco.dto.sos.tinhieu.TinHieuSOSRequestDTO;
import com.example.suco.dto.sos.tinhieu.TruSoSOSDetailResponseDTO;
import com.example.suco.dto.sos.tinhieu.UserMiniDTO;
import com.example.suco.model.User;
import com.example.suco.model.TinHieuSOS;

import org.springframework.stereotype.Component;

@Component
public class TinHieuMapper {

private UserMiniDTO toUserMiniDTO(User user) {
        
    if (user == null) {
        return null;
    }

    UserMiniDTO dto = new UserMiniDTO();

    dto.setId(user.getUid());
    dto.setName(user.getName());
    dto.setEmail(user.getEmail());
    dto.setTotalPoints(user.getTotalPoints());

    return dto;
}
            // Request DTO → Entity
        public TinHieuSOS toEntity(
        TinHieuSOSRequestDTO dto,
        String uid,
        User user
) {
    TinHieuSOS sos = new TinHieuSOS();

    // user info
    sos.setUserId(uid);
    sos.setUser(user);

    // business data
    sos.setViDo(dto.getViDo());
    sos.setKinhDo(dto.getKinhDo());
    sos.setGhiChu(dto.getGhiChu());

    // default state
    sos.setTrangThai("CHO_XU_LY");

    return sos;
}

public SOSMapResponseDTO toMapDto(TinHieuSOS sos) {

    SOSMapResponseDTO dto = new SOSMapResponseDTO();

    dto.setId(sos.getId());
    dto.setViDo(sos.getViDo());
    dto.setKinhDo(sos.getKinhDo());
    dto.setTrangThai(sos.getTrangThai());

    TruSoMiniDTO mini = new TruSoMiniDTO();
    mini.setId(sos.getIdTruSoTiepNhan()); 
    mini.setTenTruSo(null);

    dto.setTruSo(mini);

    return dto;
}
// Entity → ResponseDTO TruSo
public TruSoSOSDetailResponseDTO toTruSoDetailDto(
        TinHieuSOS sos
) {

    TruSoSOSDetailResponseDTO dto =
            new TruSoSOSDetailResponseDTO();

    dto.setId(sos.getId());
    dto.setViDo(sos.getViDo());
    dto.setKinhDo(sos.getKinhDo());
    dto.setDiaChi(sos.getDiaChi());
    dto.setGhiChu(sos.getGhiChu());
    dto.setHinhAnhUrl(sos.getHinhAnh());
    dto.setGhiAmUrl(sos.getGhiAm());
    dto.setTrangThai(sos.getTrangThai());
    dto.setThoiGianTao(sos.getCreatedAt());

    dto.setNguoiGui(toUserMiniDTO(sos.getUser()));

    return dto;
}

// Entity → ResponseDTO Admin
public AdminSOSDetailResponseDTO toAdminDetailDto(
        TinHieuSOS sos,
        TruSoMiniDTO truSoDto
) {

    AdminSOSDetailResponseDTO dto =
            new AdminSOSDetailResponseDTO();

    dto.setId(sos.getId());
    dto.setViDo(sos.getViDo());
    dto.setKinhDo(sos.getKinhDo());
    dto.setDiaChi(sos.getDiaChi());
    dto.setGhiChu(sos.getGhiChu());
    dto.setHinhAnhUrl(sos.getHinhAnh());
    dto.setGhiAmUrl(sos.getGhiAm());
    dto.setThoiGianTao(sos.getCreatedAt());
    dto.setTrangThai(sos.getTrangThai());
    dto.setUserId(sos.getUserId());
    dto.setNguoiGui(toUserMiniDTO(sos.getUser()));


    dto.setTruSoTiepNhan(truSoDto);
    return dto;
}


// Entity → ItemResponseDTO cá nhân

public TheoDoiSOSItemResponseDTO toTheoDoiItemDto(
        TinHieuSOS sos, String tenTruSo
) {

 TheoDoiSOSItemResponseDTO dto =
            new TheoDoiSOSItemResponseDTO();

    dto.setId(sos.getId());

    dto.setHinhAnh( sos.getHinhAnh() );

    dto.setTrangThai( sos.getTrangThai());

    dto.setCreatedAt(sos.getCreatedAt());

    dto.setTenTruSo(tenTruSo);


    return dto;
}


    // Entity → ResponseDTO cá nhân
    public TheoDoiSOSDetailResponseDTO toTheoDoiDto(TinHieuSOS sos, String tenTruSo){

    TheoDoiSOSDetailResponseDTO dto = new TheoDoiSOSDetailResponseDTO();

    dto.setId(sos.getId());

    dto.setViDo(sos.getViDo());
    dto.setKinhDo(sos.getKinhDo());

    dto.setDiaChi(sos.getDiaChi());

    dto.setGhiChu(sos.getGhiChu());
    dto.setHinhAnh(sos.getHinhAnh());
    dto.setGhiAm(sos.getGhiAm());

    dto.setTrangThai(sos.getTrangThai());

    dto.setCreatedAt(sos.getCreatedAt());

    dto.setIdTruSoTiepNhan(
            sos.getIdTruSoTiepNhan()
    );

    if (sos.getHoaDon() != null) {

        dto.setHoaDonId(
                sos.getHoaDon().getId()
        );

        dto.setThanhTien(
                sos.getHoaDon().getThanhTien()
        );

        dto.setTrangThaiHoaDon(
                sos.getHoaDon().getTrangThai()
        );
    }
    dto.setTenTruSoTiepNhan(tenTruSo);

    return dto;
}
}
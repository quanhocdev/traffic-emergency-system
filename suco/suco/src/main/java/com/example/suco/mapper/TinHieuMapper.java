package com.example.suco.mapper;

import com.example.suco.dto.sos.tinhieu.TheoDoiSOSDetailResponseDTO;
import com.example.suco.dto.sos.tinhieu.TinHieuSOSRequestDTO;
import com.example.suco.dto.sos.tinhieu.UserSOSDetailResponseDTO;
import com.example.suco.dto.sos.tinhieu.UserMiniDTO;
import com.example.suco.model.User;
import com.example.suco.model.TinHieuSOS;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class TinHieuMapper {

    private static final Logger log =
            LoggerFactory.getLogger(TinHieuMapper.class);


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
        // Entity → Response Map DTO
    public UserSOSDetailResponseDTO mapToDTO(TinHieuSOS sos) {

        log.info("=== MAP SOS ID={} userId={} userRelation={} ===",
                sos.getId(),
                sos.getUserId(),
                sos.getUser() != null
                        ? sos.getUser().getUid()
                        : null
        );

        UserSOSDetailResponseDTO dto =
                new UserSOSDetailResponseDTO();

        dto.setId(sos.getId());
        dto.setViDo(sos.getViDo());
        dto.setKinhDo(sos.getKinhDo());
        dto.setDiaChi(sos.getDiaChi());
        dto.setGhiChu(sos.getGhiChu());
        dto.setHinhAnh(sos.getHinhAnh());
        dto.setGhiAm(sos.getGhiAm());
        dto.setTrangThai(sos.getTrangThai());
        dto.setCreatedAt(sos.getCreatedAt());

        // ================= USER =================

        log.info("DEBUG USER ENTITY = {}", sos.getUser());

        if (sos.getUser() != null) {

            log.info("USER FOUND: uid={}, name={}",
                    sos.getUser().getUid(),
                    sos.getUser().getName()
            );

            UserMiniDTO u = new UserMiniDTO();

            u.setId(sos.getUser().getUid());
            u.setName(sos.getUser().getName());
            u.setTotalPoints(sos.getUser().getTotalPoints());
            u.setEmail(sos.getUser().getEmail());

            dto.setUser(u);

        } else {

            log.warn("USER NULL → fallback Khách vãng lai (SOS ID={})",
                    sos.getId());
        }

        return dto;
    }

    // Entity → ResponseDTO cá nhân
    public TheoDoiSOSDetailResponseDTO toTheoDoiDto(TinHieuSOS sos) {

    TheoDoiSOSDetailResponseDTO dto =
            new TheoDoiSOSDetailResponseDTO();

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

    return dto;
}
}
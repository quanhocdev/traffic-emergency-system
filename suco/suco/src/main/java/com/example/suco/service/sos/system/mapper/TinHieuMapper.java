package com.example.suco.service.sos.system.mapper;

import com.example.suco.dto.sos.TinHieuSOSResponseDTO;
import com.example.suco.dto.sos.UserMiniDTO;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.SosDieuPhoi;
import com.example.suco.repository.SosDieuPhoiRepository;

import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class TinHieuMapper {

    private static final Logger log = LoggerFactory.getLogger(TinHieuMapper.class);

    private final SosDieuPhoiRepository sosDieuPhoiRepository;

    public TinHieuMapper(SosDieuPhoiRepository sosDieuPhoiRepository) {
        this.sosDieuPhoiRepository = sosDieuPhoiRepository;
    }

    public TinHieuSOSResponseDTO mapToDTO(TinHieuSOS sos) {

        log.info("=== MAP SOS ID={} userId={} userRelation={} ===",
                sos.getId(),
                sos.getUserId(),
                sos.getUser() != null ? sos.getUser().getUid() : null
        );

        TinHieuSOSResponseDTO dto = new TinHieuSOSResponseDTO();

        dto.setId(sos.getId());
        dto.setViDo(sos.getViDo());
        dto.setKinhDo(sos.getKinhDo());
        dto.setDiaChi(sos.getDiaChi());
        dto.setGhiChu(sos.getGhiChu());
        dto.setHinhAnh(sos.getHinhAnh());
        dto.setGhiAm(sos.getGhiAm());
        dto.setTrangThai(sos.getTrangThai());
        dto.setCreatedAt(sos.getCreatedAt());

        // ================== TÍNH THỜI GIAN CÒN LẠI ==================
        Optional<SosDieuPhoi> dpOpt =
                sosDieuPhoiRepository.findTopBySosIdOrderByThoiGianGuiDesc(sos.getId());

        if (dpOpt.isPresent()) {

            SosDieuPhoi dp = dpOpt.get();

            long daTroiQua = Duration.between(
                    dp.getThoiGianGui(),
                    LocalDateTime.now()
            ).getSeconds();

            long conLai = Math.max(0, 60 - daTroiQua);

            dto.setThoiGianConLai(conLai);

            log.info("SOS {} remaining = {}s", sos.getId(), conLai);

        } else {

            dto.setThoiGianConLai(60L);

            log.warn("Không tìm thấy SosDieuPhoi cho SOS {}", sos.getId());
        }

        // ================== USER ==================

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

            log.warn("USER NULL → fallback Khách vãng lai (SOS ID={})", sos.getId());
        }

        return dto;
    }
}
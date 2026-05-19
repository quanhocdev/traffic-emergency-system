package com.example.suco.service.sos.system.mapper;

import com.example.suco.dto.sos.TinHieuSOSResponseDTO;
import com.example.suco.dto.sos.UserMiniDTO;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.User;
import com.example.suco.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class TinHieuMapper {

    private static final Logger log = LoggerFactory.getLogger(TinHieuMapper.class);

    @Autowired
    private UserRepository userRepository;

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
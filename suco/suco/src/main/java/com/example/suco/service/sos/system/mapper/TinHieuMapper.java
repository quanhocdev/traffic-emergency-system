package com.example.suco.service.sos.system.mapper;

import com.example.suco.dto.sos.TinHieuSOSResponseDTO;
import com.example.suco.dto.sos.UserMiniDTO;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.User;
import com.example.suco.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TinHieuMapper {

    @Autowired
    private UserRepository userRepository;

    public TinHieuSOSResponseDTO mapToDTO(TinHieuSOS sos) {

        TinHieuSOSResponseDTO dto = new TinHieuSOSResponseDTO();

        dto.setId(sos.getId());
        dto.setViDo(sos.getViDo());
        dto.setKinhDo(sos.getKinhDo());
        dto.setDiaChi(sos.getDiaChi());
        dto.setGhiChu(sos.getGhiChu());
        dto.setTrangThai(sos.getTrangThai());
        dto.setCreatedAt(sos.getCreatedAt());

        User user = userRepository.findById(sos.getUserId()).orElse(null);

        if (user != null) {
            UserMiniDTO u = new UserMiniDTO();
            u.setId(user.getUid());
            u.setName(user.getName());
            u.setTotalPoints(user.getTotalPoints());
            dto.setUser(u);
        }

        return dto;
    }
}
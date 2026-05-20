package com.example.suco.service.sos.user.workflow.gui.create;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.suco.dto.TinHieuSOSRequestDTO;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.User;
import com.example.suco.repository.UserRepository;

@Service
public class CreateService {

    @Autowired
    private UserRepository userRepository;

    public TinHieuSOS createSOS(String uid, TinHieuSOSRequestDTO dto) {

        TinHieuSOS sos = new TinHieuSOS();

        // Firebase UID
        sos.setUserId(uid);

        // Load User entity
        User user = userRepository.findByUid(uid).orElse(null);

        if (user != null) {
            sos.setUser(user);
        }

        sos.setViDo(dto.getViDo());
        sos.setKinhDo(dto.getKinhDo());
        sos.setGhiChu(dto.getGhiChu());
        sos.setTrangThai("CHO_XU_LY");

        return sos;
    }
}
package com.example.suco.mapper;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.suco.dto.sos.tinhieu.UserInfoResponseDTO;
import com.example.suco.service.sos.tinhieu.user.workflow.gui.VipService;
import com.example.suco.model.User;

public class InfoUserMapper {
    
    @Autowired
    private VipService vipService;


    public UserInfoResponseDTO toUserInfoResponseDTO(User user) {
        if (user == null) {
            return null;
        }
        UserInfoResponseDTO dto = new UserInfoResponseDTO();
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setVip(vipService.checkVip(user.getUid()));
        return dto;
    }

}

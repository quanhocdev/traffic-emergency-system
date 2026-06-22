package com.example.suco.mapper.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.example.suco.dto.sos.tinhieu.UserInfoResponseDTO;
import com.example.suco.dto.sos.tinhieu.UserMiniDTO;
import com.example.suco.service.sos.tinhieu.user.workflow.gui.VipService;
import com.example.suco.model.User;

@Component
public class InfoUserMapper {
    
    @Autowired
    private VipService vipService;

    // 1. Map ra DTO cha (UserInfoResponseDTO) - Dùng constructor 
    public UserInfoResponseDTO toUserInfoResponseDTO(User user) {
        if (user == null) {
            return null;
        }
        
        return new UserInfoResponseDTO(
            user.getName(),
            user.getEmail(),
            vipService.checkVip(user.getUid()),
            user.getTotalPoints()
        );
    }

    // 2. Map ra DTO con (UserMiniDTO) - Cũng gọi constructor 1 dòng nhờ tính kế thừa super()
    public UserMiniDTO toUserMiniDTO(User user) {
        if (user == null) {
            return null;
        }

        return new UserMiniDTO(
            user.getUid(),
            user.getName(),
            user.getEmail(),
            vipService.checkVip(user.getUid()),
            user.getTotalPoints()
        );
    }
}
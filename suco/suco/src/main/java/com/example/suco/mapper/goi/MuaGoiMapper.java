package com.example.suco.mapper.goi;

import com.example.suco.dto.sos.goi.dangky.MuaGoiRequestDTO;
import com.example.suco.dto.sos.goi.dangky.MuaGoiUserResponseDTO;
import com.example.suco.dto.sos.goi.quanly.GoiResponseDTO;

import com.example.suco.model.Goi;
import com.example.suco.model.MuaGoi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component 
public class MuaGoiMapper {

    @Autowired
    private GoiMapper goimapper;

    // REQUEST -> ENTITY
    public MuaGoi toEntity(MuaGoiRequestDTO dto, String userId, Goi goi) {
        if (dto == null || goi == null) {
            return null;
        }
        
        MuaGoi entity = new MuaGoi();

        entity.setUserId(userId);
        entity.setGoiId(dto.getGoiId());

        LocalDateTime now = LocalDateTime.now();
        entity.setNgayMua(now);
        entity.setTrangThai("PENDING");

        entity.setNgayHetHan(
                now.plusDays(goi.getThoiHan())
        );

        return entity;
    }

    // ENTITY -> RESPONSE user 
    public MuaGoiUserResponseDTO toResponse(MuaGoi entity, Goi goi) {

        if (entity == null) {
            return null;
        }

        GoiResponseDTO goiResponse = goimapper.toResponseDTO(goi);

        return new MuaGoiUserResponseDTO(
                goiResponse,
                entity.getNgayMua(),
                entity.getNgayHetHan(),
                entity.getTrangThai()
        );
    }
}
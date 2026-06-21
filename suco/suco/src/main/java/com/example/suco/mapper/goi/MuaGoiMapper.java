package com.example.suco.mapper.goi;

import com.example.suco.dto.sos.goi.dangky.MuaGoiRequestDTO;
import com.example.suco.dto.sos.goi.dangky.MuaGoiResponseDTO;
import com.example.suco.model.Goi;
import com.example.suco.model.MuaGoi;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component 
public class MuaGoiMapper {

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

    // ENTITY -> RESPONSE 
    public MuaGoiResponseDTO toResponse(MuaGoi entity, String tenGoi) {
        if (entity == null) {
            return null; // Null-check an toàn
        }
        
        return new MuaGoiResponseDTO(
                entity.getUserId(),
                tenGoi,
                entity.getNgayMua(),
                entity.getNgayHetHan(),
                entity.getTrangThai()
        );
    }
}
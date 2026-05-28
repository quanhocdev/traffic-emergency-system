package com.example.suco.mapper;

import com.example.suco.dto.sos.goi.payment.MuaGoiRequestDTO;
import com.example.suco.dto.sos.goi.payment.MuaGoiResponseDTO;
import com.example.suco.model.Goi;
import com.example.suco.model.MuaGoi;

import java.time.LocalDateTime;

public class MuaGoiMapper {

    // REQUEST -> ENTITY
    public static MuaGoi toEntity(MuaGoiRequestDTO dto, String userId, Goi goi) {
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
    public static MuaGoiResponseDTO toResponse(MuaGoi entity, String tenGoi) {
        return new MuaGoiResponseDTO(
                entity.getUserId(),
                tenGoi,
                entity.getNgayMua(),
                entity.getNgayHetHan(),
                entity.getTrangThai()
        );
    }
}
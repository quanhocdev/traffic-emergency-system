package com.example.suco.mapper;

import com.example.suco.dto.sos.goi.quanly.GoiRequestDTO;
import com.example.suco.dto.sos.goi.quanly.GoiResponseDTO;
import com.example.suco.model.Goi;

public class GoiMapper {

    // RequestDTO -> Entity
    public static Goi toEntity(GoiRequestDTO dto) {

        Goi goi = new Goi();

        goi.setTen(dto.getTen());
        goi.setGia(dto.getGia());
        goi.setThoiHan(dto.getThoiHan());
        goi.setKhoangCachMienPhi(dto.getKhoangCachMienPhi());
        goi.setUuDai(dto.getUuDai());

        return goi;
    }

        // Update Entity with RequestDTO 
    public static void updateEntity(Goi goi, GoiRequestDTO dto) {

        if (dto.getTen() != null) {
            goi.setTen(dto.getTen());
        }

        if (dto.getGia() != null) {
            goi.setGia(dto.getGia());
        }

        if (dto.getThoiHan() != null) {
            goi.setThoiHan(dto.getThoiHan());
        }

        if (dto.getKhoangCachMienPhi() != null) {
            goi.setKhoangCachMienPhi(dto.getKhoangCachMienPhi());
        }

        if (dto.getUuDai() != null) {
            goi.setUuDai(dto.getUuDai());
        }
    }

    // Entity -> ResponseDTO
    public static GoiResponseDTO toResponseDTO(Goi goi) {

        GoiResponseDTO dto = new GoiResponseDTO();

        dto.setId(goi.getId());
        dto.setTen(goi.getTen());
        dto.setGia(goi.getGia());
        dto.setThoiHan(goi.getThoiHan());
        dto.setKhoangCachMienPhi(goi.getKhoangCachMienPhi());
        dto.setUuDai(goi.getUuDai());

        return dto;
    }
}
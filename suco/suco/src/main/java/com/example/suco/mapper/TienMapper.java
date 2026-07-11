package com.example.suco.mapper;

import com.example.suco.dto.tienich.tien.quydoi.DoiTienRequestDTO;
import com.example.suco.dto.tienich.tien.quydoi.DoiTienResponseDTO;
import com.example.suco.model.DoiTien;
import org.springframework.stereotype.Component;

@Component
public class TienMapper {

    /**
     * RequestDTO -> Entity
     */
    public DoiTien toEntity(DoiTienRequestDTO request) {

        DoiTien entity = new DoiTien();

        entity.setSoDiemDoi(request.getSoDiemDoi());
        entity.setLoaiDoi(request.getLoaiDoi());

        return entity;
    }

    /**
     * Entity -> ResponseDTO
     */
    public DoiTienResponseDTO toResponseDTO(DoiTien entity) {

        DoiTienResponseDTO dto = new DoiTienResponseDTO();

        dto.setId(entity.getId());
        dto.setSoDiemDoi(entity.getSoDiemDoi());
        dto.setGiaTri(entity.getGiaTri());
        dto.setLoaiDoi(entity.getLoaiDoi());

        if (entity.getNgayDoi() != null) {
            dto.setNgayDoi(entity.getNgayDoi());
        }

        return dto;
    }
}
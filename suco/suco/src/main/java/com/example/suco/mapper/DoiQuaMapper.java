package com.example.suco.mapper;

import java.time.LocalDateTime;

import com.example.suco.dto.tienich.qua.quydoi.DoiQuaRequestDTO;
import com.example.suco.dto.tienich.qua.quydoi.DoiQuaResponseDTO;
import com.example.suco.dto.tienich.qua.quydoi.TuiQuaResponseDTO;
import com.example.suco.model.DoiQua;
import com.example.suco.model.Qua;
import com.example.suco.model.TuiQua;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component // 1. Chuyển thành Spring Bean
public class DoiQuaMapper {

    // 2. Inject QuaMapper (đã thành Component ở bước trước) vào để tái sử dụng
    @Autowired
    private QuaMapper quaMapper;

    // Request -> Entity DoiQua (Lịch sử đổi quà)
    public DoiQua toDoiQuaEntity(DoiQuaRequestDTO dto, String userId, Integer diemDaTru) {
        if (dto == null) return null; // Null-check an toàn

        DoiQua doiQua = new DoiQua();
        doiQua.setUserId(userId);
        doiQua.setQuaId(dto.getQuaId());
        doiQua.setSoLuong(dto.getSoLuong());
        doiQua.setDiemDaTru(diemDaTru);
        doiQua.setNgayDoi(LocalDateTime.now());

        return doiQua;
    }

    // Entity DoiQua -> DoiQuaResponseDTO
    public DoiQuaResponseDTO toDoiQuaResponse(DoiQua doiQua, Qua qua) {
        if (doiQua == null) return null;

        DoiQuaResponseDTO dto = new DoiQuaResponseDTO();
        dto.setId(doiQua.getId());
        
        // Gọi qua instance thay vì gọi hàm static cũ
        dto.setQua(quaMapper.toResponseDTO(qua)); 
        
        dto.setSoLuong(doiQua.getSoLuong());
        dto.setDiemDaTru(doiQua.getDiemDaTru());
        dto.setNgayDoi(doiQua.getNgayDoi());

        return dto;
    }
    
    // Entity TuiQua -> TuiQuaResponseDTO
    public TuiQuaResponseDTO toTuiQuaResponse(TuiQua tuiQua, Qua qua) {
        if (tuiQua == null) return null;

        TuiQuaResponseDTO dto = new TuiQuaResponseDTO();
        dto.setSoLuong(tuiQua.getSoLuong());
        
        // Gọi qua instance thay vì gọi hàm static cũ
        dto.setQua(quaMapper.toResponseDTO(qua)); 
        
        return dto;
    }
}
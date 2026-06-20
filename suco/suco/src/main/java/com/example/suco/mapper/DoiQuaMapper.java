package com.example.suco.mapper;

import java.time.LocalDateTime;

import com.example.suco.dto.tienich.qua.quydoi.DoiQuaRequestDTO;
import com.example.suco.dto.tienich.qua.quydoi.DoiQuaResponseDTO;
import com.example.suco.dto.tienich.qua.quydoi.TuiQuaResponseDTO;
import com.example.suco.model.DoiQua;
import com.example.suco.model.Qua;
import com.example.suco.model.TuiQua;

public class DoiQuaMapper {

    // Request -> Entity DoiQua (lịch sử đổi quà)
    public static DoiQua toDoiQuaEntity(
            DoiQuaRequestDTO dto,
            String userId,
            Integer diemDaTru) {

        DoiQua doiQua = new DoiQua();

        doiQua.setUserId(userId);
        doiQua.setQuaId(dto.getQuaId());
        doiQua.setSoLuong(dto.getSoLuong());
        doiQua.setDiemDaTru(diemDaTru);
        doiQua.setNgayDoi(LocalDateTime.now());

        return doiQua;
    }

      // Entity DoiQua -> DoiQuaResponseDTO
    public static DoiQuaResponseDTO toDoiQuaResponse(
            DoiQua doiQua,
            Qua qua) {

        DoiQuaResponseDTO dto = new DoiQuaResponseDTO();

        dto.setId(doiQua.getId());
        dto.setQua(QuaMapper.toResponseDTO(qua));
        dto.setSoLuong(doiQua.getSoLuong());
        dto.setDiemDaTru(doiQua.getDiemDaTru());
        dto.setNgayDoi(doiQua.getNgayDoi());

        return dto;
    }
    
    // Entity TuiQua -> TuiQuaResponseDTO
    public static TuiQuaResponseDTO toTuiQuaResponse(
            TuiQua tuiQua,
            Qua qua) {

        TuiQuaResponseDTO dto = new TuiQuaResponseDTO();
        dto.setSoLuong(tuiQua.getSoLuong());
        dto.setQua(QuaMapper.toResponseDTO(qua));
        return dto;
    }
}
package com.example.suco.mapper.qua;

import java.time.LocalDateTime;

import com.example.suco.dto.tienich.qua.quydoi.DoiQuaRequestDTO;
import com.example.suco.dto.tienich.qua.quydoi.DoiQuaResponseDTO;
import com.example.suco.dto.tienich.qua.quydoi.TuiQuaResponseDTO;
import com.example.suco.model.DoiQua;
import com.example.suco.model.TuiQua;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component 
public class DoiQuaMapper {

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
    public DoiQuaResponseDTO toDoiQuaResponse(DoiQua doiQua) {
    if (doiQua == null) return null;

    DoiQuaResponseDTO dto = new DoiQuaResponseDTO();
    dto.setId(doiQua.getId());
    dto.setQua(quaMapper.toResponseDTO(doiQua.getQua()));
    dto.setSoLuong(doiQua.getSoLuong());
    dto.setDiemDaTru(doiQua.getDiemDaTru());
    dto.setNgayDoi(doiQua.getNgayDoi());

    return dto;
}
    
    // Entity TuiQua -> TuiQuaResponseDTO
    public TuiQuaResponseDTO toTuiQuaResponse(TuiQua tuiQua) {
    if (tuiQua == null) return null;

    TuiQuaResponseDTO dto = new TuiQuaResponseDTO();
    dto.setSoLuong(tuiQua.getSoLuong());
    dto.setQua(quaMapper.toResponseDTO(tuiQua.getQua()));

    return dto;
}
}
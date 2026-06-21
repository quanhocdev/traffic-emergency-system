package com.example.suco.mapper.qua;

import com.example.suco.dto.tienich.qua.quanly.QuaRequestDTO;
import com.example.suco.dto.tienich.qua.quanly.QuaResponseDTO;
import com.example.suco.model.Qua;
import org.springframework.stereotype.Component;

@Component 
public class QuaMapper {

    // Request DTO -> Entity
    public Qua toEntity(QuaRequestDTO requestDTO) {
        if (requestDTO == null) {
            return null;
        }
        Qua qua = new Qua();
        qua.setTen(requestDTO.getTen());
        qua.setLoai(requestDTO.getLoai());
        qua.setMoTa(requestDTO.getMoTa());
        qua.setDiem(requestDTO.getDiem());
        qua.setGiaTriGiamPercent(requestDTO.getGiaTriGiamPercent());
        qua.setGiaTriToiDa(requestDTO.getGiaTriToiDa());
        qua.setNgayKetThuc(requestDTO.getNgayKetThuc());
        
        // Nếu request không truyền trạng thái, mặc định là HOAT_DONG
        if (requestDTO.getTrangThai() != null) {
            qua.setTrangThai(requestDTO.getTrangThai());
        } 

        return qua;
    }

    // Entity -> Response DTO 
    public QuaResponseDTO toResponseDTO(Qua qua) {
        if (qua == null) {
            return null;
        }

        QuaResponseDTO responseDTO = new QuaResponseDTO();
        responseDTO.setId(qua.getId()); 
        responseDTO.setTen(qua.getTen());
        responseDTO.setLoai(qua.getLoai());
        responseDTO.setMoTa(qua.getMoTa());
        responseDTO.setDiem(qua.getDiem());
        responseDTO.setHinhAnh(qua.getHinhAnh()); 
        responseDTO.setGiaTriGiamPercent(qua.getGiaTriGiamPercent());
        responseDTO.setGiaTriToiDa(qua.getGiaTriToiDa());
        responseDTO.setNgayKetThuc(qua.getNgayKetThuc());
        responseDTO.setTrangThai(qua.getTrangThai());

        return responseDTO;
    }
}
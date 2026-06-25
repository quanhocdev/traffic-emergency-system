package com.example.suco.mapper.info;

import org.springframework.stereotype.Component;

import com.example.suco.dto.info.truso.TruSoMapDto;
import com.example.suco.dto.info.truso.TruSoMiniDTO;
import com.example.suco.dto.vanhanh.truso.TruSoCreateRequestDTO;
import com.example.suco.model.TruSo;

@Component
public class InfoTruSoMapper {

    // Admin tạo trụ sở mới, map từ DTO sang entity
    public TruSo toEntity(TruSoCreateRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        TruSo truSo = new TruSo();
        truSo.setTenDangNhap(dto.getTenDangNhap());
        truSo.setMatKhau(dto.getMatKhau());
        truSo.setTenTruSo(dto.getTenTruSo());
        truSo.setKinhDo(dto.getKinhDo());
        truSo.setViDo(dto.getViDo());
        return truSo;
    }

    // 1. Map ra TruSoMiniDTO (chỉ có id, tên)
    public TruSoMiniDTO toMiniDto(TruSo truSo) {
        if (truSo == null) return null;
        
        return new TruSoMiniDTO(truSo.getId(), truSo.getTenTruSo());
    }

    // 2. Map ra TruSoMapDto (đầy đủ tọa độ, địa chỉ nhờ constructor kế thừa)
    public TruSoMapDto toMapDto(TruSo truSo) {
        if (truSo == null) return null;
        return new TruSoMapDto(
                truSo.getId(),
                truSo.getTenTruSo(),
                truSo.getKinhDo(),
                truSo.getViDo(),
                truSo.getDiaChi()
        );
    }
}
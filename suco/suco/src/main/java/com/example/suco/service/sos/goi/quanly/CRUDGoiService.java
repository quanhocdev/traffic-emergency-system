package com.example.suco.service.sos.goi.quanly;

import com.example.suco.dto.sos.goi.quanly.GoiRequestDTO;
import com.example.suco.dto.sos.goi.quanly.GoiResponseDTO;
import com.example.suco.mapper.GoiMapper;
import com.example.suco.model.Goi;
import com.example.suco.repository.sos.goi.CRUDGoiRepository;
import com.example.suco.service.sos.goi.quanly.validation.ValidationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CRUDGoiService {

    @Autowired
    private CRUDGoiRepository goiRepository;

    @Autowired
    private ValidationService validationService;

    // Lấy danh sách gói và chuyển sang DTO
   public List<GoiResponseDTO> getAllGoi() {

    return goiRepository.findAll()
        .stream()
        .map(GoiMapper::toResponseDTO)
        .toList();
}

    public void deleteGoi(Long id) {
    Goi goi = goiRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Gói không tồn tại"));

    goiRepository.delete(goi);
}

public GoiResponseDTO createGoi(GoiRequestDTO dto) {

    validationService.validateCreate(dto);

    Goi goi = GoiMapper.toEntity(dto);

    Goi saved = goiRepository.save(goi);

    return GoiMapper.toResponseDTO(saved);
}

public GoiResponseDTO updateGoi(Long id, GoiRequestDTO dto) {

    validationService.validateUpdate(dto);

    Goi goi = goiRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Không tìm thấy gói"));

    GoiMapper.updateEntity(goi, dto);

    Goi updated = goiRepository.save(goi);

    return GoiMapper.toResponseDTO(updated);
}
}
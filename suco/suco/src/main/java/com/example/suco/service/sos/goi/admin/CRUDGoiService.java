package com.example.suco.service.sos.goi.admin;

import com.example.suco.dto.sos.goi.quanly.GoiRequestDTO;
import com.example.suco.dto.sos.goi.quanly.GoiResponseDTO;
import com.example.suco.mapper.goi.GoiMapper;
import com.example.suco.model.Goi;
import com.example.suco.repository.sos.goi.CRUDGoiRepository;
import com.example.suco.service.sos.goi.admin.validation.ValidationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CRUDGoiService {

    @Autowired
    private CRUDGoiRepository goiRepository;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private GoiMapper goiMapper;

    // Lấy danh sách gói và chuyển sang DTO
    public List<GoiResponseDTO> getAllGoi() {
        return goiRepository.findAll()
            .stream()
            .map(goiMapper::toResponseDTO) 
            .toList();
    }

    public void deleteGoi(Long id) {
        Goi goi = goiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gói không tồn tại"));

        goiRepository.delete(goi);
    }

    public GoiResponseDTO createGoi(GoiRequestDTO dto) {
        validationService.validateCreate(dto);

        // 2. Sửa sang gọi qua instance bean
        Goi goi = goiMapper.toEntity(dto);

        Goi saved = goiRepository.save(goi);

        return goiMapper.toResponseDTO(saved);
    }

    public GoiResponseDTO updateGoi(Long id, GoiRequestDTO dto) {
        validationService.validateUpdate(dto);

        Goi goi = goiRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy gói"));

        goiMapper.updateEntity(goi, dto);

        Goi updated = goiRepository.save(goi);

        return goiMapper.toResponseDTO(updated);
    }
}
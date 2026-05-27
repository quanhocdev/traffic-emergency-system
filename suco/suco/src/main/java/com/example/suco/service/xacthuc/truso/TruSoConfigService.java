package com.example.suco.service.xacthuc.truso;

import org.springframework.stereotype.Service;

import com.example.suco.dto.truso.TruSoConfigDTO;
import com.example.suco.model.TruSo;
import com.example.suco.repository.xacthuc.TruSoRepository;

@Service
public class TruSoConfigService {

    private final TruSoRepository truSoRepository;

    public TruSoConfigService(TruSoRepository truSoRepository) {
        this.truSoRepository = truSoRepository;
    }

    public TruSo updateConfig(Long truSoId, TruSoConfigDTO dto) {

        TruSo truSo = truSoRepository.findById(truSoId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy trụ sở"));

        if (dto.getTrangThaiHoatDong() != null) {
            truSo.setTrangThaiHoatDong(dto.getTrangThaiHoatDong());
        }

        return truSoRepository.save(truSo);
    }
}
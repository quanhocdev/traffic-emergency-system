package com.example.suco.service.tienich.qua.admin.validation;

import com.example.suco.dto.tienich.qua.quanly.QuaRequestDTO;
import org.springframework.stereotype.Service;

@Service
public class ValidateService {

    public void validateCreate(QuaRequestDTO dto) {

        if (dto.getDiem() == null) {
            throw new IllegalArgumentException(
                    "Điểm đổi quà không được để trống");
        }

        if (dto.getDiem() <= 0) {
            throw new IllegalArgumentException(
                    "Điểm đổi quà phải lớn hơn 0");
        }
    }

    public void validateUpdate(QuaRequestDTO dto) {

        if (dto.getDiem() != null
                && dto.getDiem() <= 0) {

            throw new IllegalArgumentException(
                    "Điểm đổi quà phải lớn hơn 0");
        }
    }
}
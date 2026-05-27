package com.example.suco.service.sos.goi.validation;

import com.example.suco.dto.sos.goi.GoiRequestDTO;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class ValidationService {

    public void validateCreate(GoiRequestDTO dto) {

        if (dto.getTen() == null || dto.getTen().isBlank()) {
            throw new RuntimeException("Tên gói không được để trống");
        }

        if (dto.getGia() == null) {
            throw new RuntimeException("Giá không được để trống");
        }

        if (dto.getGia().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Giá phải lớn hơn 0");
        }

        if (dto.getThoiHan() == null) {
            throw new RuntimeException("Thời hạn không được để trống");
        }

        if (dto.getThoiHan() <= 0) {
            throw new RuntimeException("Thời hạn phải lớn hơn 0");
        }

        if (dto.getKhoangCachMienPhi() == null) {
            throw new RuntimeException(
                "Khoảng cách miễn phí không được để trống"
            );
        }

        if (dto.getKhoangCachMienPhi() < 0) {
            throw new RuntimeException(
                "Khoảng cách miễn phí phải lớn hơn hoặc bằng 0"
            );
        }
    }

    public void validateUpdate(GoiRequestDTO dto) {

        if (dto.getGia() != null &&
            dto.getGia().compareTo(BigDecimal.ZERO) <= 0) {

            throw new RuntimeException("Giá phải lớn hơn 0");
        }

        if (dto.getThoiHan() != null &&
            dto.getThoiHan() <= 0) {

            throw new RuntimeException("Thời hạn phải lớn hơn 0");
        }

        if (dto.getKhoangCachMienPhi() != null &&
            dto.getKhoangCachMienPhi() < 0) {

            throw new RuntimeException(
                "Khoảng cách miễn phí phải lớn hơn hoặc bằng 0"
            );
        }
    }
}
package com.example.suco.service.xacthuc.truso;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.suco.model.TruSo;
import com.example.suco.repository.vanhanh.TruSoRepository;

@Service
public class ValidationInfoTruSoService {

    @Autowired
    private TruSoRepository truSoRepository;

    public void validateUsername(TruSo truSo) {

        String username = truSo.getTenDangNhap();

        boolean isDuplicate =
                truSoRepository.existsByTenDangNhap(username);

        // CREATE
        if (truSo.getId() == null) {

            if (isDuplicate) {
                throw new RuntimeException("Tên đăng nhập đã tồn tại");
            }

            return;
        }

        // UPDATE
        TruSo existing = truSoRepository.findById(truSo.getId())
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy trụ sở"));

        if (!existing.getTenDangNhap().equals(username)
                && isDuplicate) {

            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }
    }
}
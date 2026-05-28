package com.example.suco.service.suco.baocao.user.workflow.gui.create;

import com.example.suco.dto.suco.baocao.SuCoRequestDTO;
import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.model.LoaiSuCo;
import com.example.suco.model.User;
import com.example.suco.repository.suco.loai.LoaiSuCoRepository;
import com.example.suco.repository.vanhanh.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreateBaoCaoService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoaiSuCoRepository loaiSuCoRepository;

    public BaoCaoSuCo create(
            String uid,
            SuCoRequestDTO dto
    ) {

        User user = userRepository.findById(uid)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        LoaiSuCo loaiSuCo = loaiSuCoRepository.findById(dto.getLoaiSuCoId())
                .orElseThrow(() -> new RuntimeException("Loại sự cố không tồn tại"));

        BaoCaoSuCo report = new BaoCaoSuCo();

        report.setReporter(user);
        report.setLoaiSuCo(loaiSuCo);

        report.setMoTa(dto.getMoTa());
        report.setViDo(dto.getViDo());
        report.setKinhDo(dto.getKinhDo());
        report.setHinhAnhUrl(dto.getHinhAnhUrl());

        return report;
    }
}
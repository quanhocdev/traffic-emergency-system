package com.example.suco.service.suco.baocao.system.builder;
import com.example.suco.dto.suco.baocao.SuCoResponseDTO;
import com.example.suco.dto.vanhanh.truso.TruSoMapDto;
import com.example.suco.mapper.SuCoMapper;
import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.model.TruSo;
import com.example.suco.repository.xacthuc.TruSoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SuCoResponseBuilder {

    @Autowired
    private TruSoRepository truSoRepository;

    @Autowired
    private SuCoMapper suCoMapper;

    public SuCoResponseDTO buildSuCoDto(BaoCaoSuCo b) {

        SuCoResponseDTO dto = suCoMapper.toDto(b);

        if (b.getReporter() != null) {
            dto.setReporterUid(b.getReporter().getUid());
        }

        if (b.getTruSoDeXuat() != null) {
            TruSo ts = truSoRepository.findById(b.getTruSoDeXuat().getId()).orElse(null);
            if (ts != null) {
                dto.setTruSoDeXuat(
                        new TruSoMapDto(
                                ts.getId(),
                                ts.getTenTruSo(),
                                ts.getKinhDo(),
                                ts.getViDo()
                        )
                );
            }
        }

        if (b.getTruSoTiepNhan() != null) {
            TruSo ts = truSoRepository.findById(b.getTruSoTiepNhan().getId()).orElse(null);
            if (ts != null) {
                dto.setTruSoTiepNhan(
                        new TruSoMapDto(
                                ts.getId(),
                                ts.getTenTruSo(),
                                ts.getKinhDo(),
                                ts.getViDo()
                        )
                );
            }
        }

        return dto;
    }
}
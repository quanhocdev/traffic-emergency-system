package com.example.suco.service.suco.baocao.user.workflow.gui;

import com.example.suco.dto.suco.baocao.ai.AiVerifyResult;
import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.model.LoaiSuCo;
import com.example.suco.repository.suco.loai.LoaiSuCoRepository;
import com.example.suco.service.suco.baocao.system.validation.ai.AiVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BaoCaoAiService {

    @Autowired
    private AiVerificationService aiVerificationService;

    @Autowired
    private LoaiSuCoRepository loaiSuCoRepository;

    public AiVerifyResult verify(
            BaoCaoSuCo report,
            String base64
    ) {

        LoaiSuCo loaiSuCo =
                loaiSuCoRepository.findById(
                        report.getLoaiSuCo().getId()
                ).orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Loại sự cố không tồn tại"
                        )
                );

        report.setLoaiSuCo(loaiSuCo);

        return aiVerificationService.verifyImage(
                base64,
                loaiSuCo.getTen()
        );
    }
}
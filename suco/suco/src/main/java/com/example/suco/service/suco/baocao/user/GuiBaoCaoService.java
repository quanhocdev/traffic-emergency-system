package com.example.suco.service.suco.baocao.user;

import com.example.suco.dto.suco.baocao.ai.AiResponse;
import com.example.suco.dto.suco.baocao.ai.AiVerifyResult;
import com.example.suco.dto.suco.baocao.user.SuCoRequestDTO;
import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.model.enums.TrangThaiXuLy;
import com.example.suco.service.dieuphoi.TruSoSelectorService;
import com.example.suco.service.suco.baocao.user.workflow.gui.BaoCaoAiService;
import com.example.suco.service.suco.baocao.user.workflow.gui.BaoCaoEnrichService;
import com.example.suco.service.suco.baocao.user.workflow.gui.BaoCaoResponseFactory;
import com.example.suco.service.suco.baocao.user.workflow.gui.BaoCaoRewardService;
import com.example.suco.service.suco.baocao.user.workflow.gui.CreateBaoCaoService;
import com.example.suco.service.suco.baocao.user.workflow.gui.DuplicateBaoCaoService;
import com.example.suco.repository.suco.baocao.SuCoAdminRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GuiBaoCaoService {

    @Autowired
    private CreateBaoCaoService createBaoCaoService;
    @Autowired
    private BaoCaoAiService baoCaoAiService;

    @Autowired
    private DuplicateBaoCaoService duplicateBaoCaoService;

    @Autowired
    private BaoCaoEnrichService baoCaoEnrichService;

    @Autowired
    private BaoCaoRewardService baoCaoRewardService;

    @Autowired
    private BaoCaoResponseFactory baoCaoResponseFactory;

    @Autowired
    private SuCoAdminRepository reportRepository;

@Autowired
    private TruSoSelectorService truSoSelectorService;


    @Transactional
public AiResponse submitReport(
        String uid,
        SuCoRequestDTO dto,
        String base64FullData
) {

    // 1. CREATE
    BaoCaoSuCo report =
            createBaoCaoService.create(uid, dto);

    // 2. AI VERIFY
    AiVerifyResult ai =
            baoCaoAiService.verify(
                    report,
                    base64FullData
            );

    if (!ai.isValid()) {
        return baoCaoResponseFactory.reject(ai);
    }

    // 3. DUPLICATE CHECK
    AiResponse duplicateResponse =
            duplicateBaoCaoService.process(uid, report);

    if (duplicateResponse != null) {
        return duplicateResponse;
    }

    // 4. ENRICH + SAVE BASE DATA
    BaoCaoSuCo savedReport =
            baoCaoEnrichService.enrichAndSave(
                    report,
                    base64FullData
            );

    // 5. AI ASSIGN TRỤ SỞ 
    var truSo = truSoSelectorService.selectNearest(
            savedReport.getViDo(),
            savedReport.getKinhDo()
    );

    savedReport.setTruSoTiepNhan(truSo);

    // 6. SET STATE
    savedReport.setTrangThaiXuLy(
            TrangThaiXuLy.DA_TIEP_NHAN
    );

    // 7. SAVE FINAL STATE
    BaoCaoSuCo finalReport =
            reportRepository.save(savedReport);

    // 8. RESPONSE + OPTIONAL REWARD 
    return baoCaoResponseFactory.success(finalReport);
}
}
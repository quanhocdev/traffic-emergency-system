package com.example.suco.service.suco.baocao.user;

import com.example.suco.dto.suco.baocao.SuCoRequestDTO;
import com.example.suco.dto.suco.baocao.ai.AiResponse;
import com.example.suco.dto.suco.baocao.ai.AiVerifyResult;
import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.model.enums.TrangThaiXuLy;
import com.example.suco.mapper.SuCoMapper;
import com.example.suco.service.suco.baocao.user.workflow.gui.ai.BaoCaoAiService;
import com.example.suco.service.suco.baocao.user.workflow.gui.create.CreateBaoCaoService;
import com.example.suco.service.suco.baocao.user.workflow.gui.duplicate.DuplicateBaoCaoService;
import com.example.suco.service.suco.baocao.user.workflow.gui.enrich.BaoCaoEnrichService;
import com.example.suco.service.suco.baocao.user.workflow.gui.response.BaoCaoResponseFactory;
import com.example.suco.service.suco.baocao.user.workflow.gui.reward.BaoCaoRewardService;
    import com.example.suco.service.dieuphoi.decision.TruSoSelectorService;
import com.example.suco.repository.suco.baocao.BaoCaoSuCoRepository;

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
    private BaoCaoSuCoRepository reportRepository;

    @Autowired
    private SuCoMapper suCoMapper;

@Autowired
    private TruSoSelectorService truSoSelectorService;


    @Transactional
public AiResponse submitReport(
        String uid,
        SuCoRequestDTO dto,
        String base64FullData
) {

    // =========================
    // 1. CREATE
    // =========================
    BaoCaoSuCo report =
            createBaoCaoService.create(uid, dto);

    // =========================
    // 2. AI VERIFY
    // =========================
    AiVerifyResult ai =
            baoCaoAiService.verify(
                    report,
                    base64FullData
            );

    if (!ai.isValid()) {
        return baoCaoResponseFactory.reject(ai);
    }

    // =========================
    // 3. DUPLICATE CHECK
    // =========================
    AiResponse duplicateResponse =
            duplicateBaoCaoService.process(uid, report);

    if (duplicateResponse != null) {
        return duplicateResponse;
    }

    // =========================
    // 4. ENRICH + SAVE BASE DATA
    // =========================
    BaoCaoSuCo savedReport =
            baoCaoEnrichService.enrichAndSave(
                    report,
                    base64FullData
            );

    // =========================
    // 5. AI ASSIGN TRỤ SỞ (QUAN TRỌNG)
    // =========================
    var truSo = truSoSelectorService.selectNearest(
            savedReport.getViDo(),
            savedReport.getKinhDo()
    );

    savedReport.setTruSoTiepNhan(truSo);

    // =========================
    // 6. SET STATE
    // =========================
    savedReport.setTrangThaiXuLy(
            TrangThaiXuLy.DA_TIEP_NHAN
    );

    // =========================
    // 7. SAVE FINAL STATE
    // =========================
    BaoCaoSuCo finalReport =
            reportRepository.save(savedReport);

    // =========================
    // 8. RESPONSE + OPTIONAL REWARD (KHÔNG NÊN REWARD Ở ĐÂY)
    // =========================
    return baoCaoResponseFactory.success(finalReport);
}
}
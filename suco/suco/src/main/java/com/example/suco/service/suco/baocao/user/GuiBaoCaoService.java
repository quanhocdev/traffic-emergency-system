package com.example.suco.service.suco.baocao.user;

import com.example.suco.dto.suco.baocao.ai.AiResponse;
import com.example.suco.dto.suco.baocao.ai.AiVerifyResult;
import com.example.suco.dto.suco.baocao.user.SuCoRequestDTO;
import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.model.LoaiSuCo;
import com.example.suco.model.enums.TrangThaiXuLy;
import com.example.suco.service.dieuphoi.TruSoSelectorService;
import com.example.suco.service.suco.baocao.system.reward.NewReportRewardPolicy;
import com.example.suco.service.suco.baocao.system.reward.RewardEngine;
import com.example.suco.service.suco.baocao.user.workflow.gui.BaoCaoAiService;
import com.example.suco.service.suco.baocao.user.workflow.gui.BaoCaoEnrichService;
import com.example.suco.service.suco.baocao.user.workflow.gui.BaoCaoResponseFactory;
import com.example.suco.service.suco.baocao.user.workflow.gui.CreateBaoCaoService;
import com.example.suco.service.suco.baocao.user.workflow.gui.DuplicateBaoCaoService;
import com.example.suco.service.suco.loai.LoaiSuCoService;
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
        private RewardEngine rewardEngine;

    @Autowired
    private BaoCaoResponseFactory baoCaoResponseFactory;

    @Autowired
    private SuCoAdminRepository reportRepository;

@Autowired
    private TruSoSelectorService truSoSelectorService;
    @Autowired
private LoaiSuCoService loaiSuCoService;


    @Transactional
public AiResponse submitReport(String uid, SuCoRequestDTO dto, String base64FullData) {

        // Lấy loại sự cố từ cơ sở dữ liệu
        LoaiSuCo loaiSuCo = loaiSuCoService.findById(dto.getLoaiSuCoId());


    // 1. CREATE
    BaoCaoSuCo report = createBaoCaoService.create(uid, dto, loaiSuCo);

    // 2. AI VERIFY
    AiVerifyResult ai = baoCaoAiService.verify(report, base64FullData);

    if (!ai.isValid()) {
        return baoCaoResponseFactory.reject(ai);
    }

    // 3. DUPLICATE CHECK
    AiResponse duplicateResponse =
            duplicateBaoCaoService.process(uid, report);

    if (duplicateResponse != null) {
        return duplicateResponse;
    }

    // 4. ENRICH
    BaoCaoSuCo savedReport =
            baoCaoEnrichService.enrichAndSave(report, base64FullData);

    // 5. TRỤ SỞ
    var truSo = truSoSelectorService.selectNearest(
            savedReport.getViDo(),
            savedReport.getKinhDo()
    );

    savedReport.setTruSoTiepNhan(truSo);
    savedReport.setTrangThaiXuLy(TrangThaiXuLy.DA_TIEP_NHAN);

    // 6. SAVE
    BaoCaoSuCo finalReport = reportRepository.save(savedReport);

    // 7. REWARD (chỉ NEW REPORT)
    rewardEngine.reward(uid, new NewReportRewardPolicy());

    // 8. RESPONSE
    return baoCaoResponseFactory.success(finalReport);
}
}
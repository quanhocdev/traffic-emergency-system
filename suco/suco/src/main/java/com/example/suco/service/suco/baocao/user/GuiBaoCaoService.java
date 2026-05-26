package com.example.suco.service.suco.baocao.user;

import com.example.suco.dto.suco.baocao.user.request.BaoCaoRequest;
import com.example.suco.dto.BaoCaoResponse;
import com.example.suco.dto.SuCoMapDto;
import com.example.suco.model.BaoCaoSuCo;

import com.example.suco.service.AiVerifyResult;

import com.example.suco.service.suco.baocao.system.builder.SuCoResponseBuilder;

import com.example.suco.service.suco.baocao.user.workflow.gui.ai.BaoCaoAiService;
import com.example.suco.service.suco.baocao.user.workflow.gui.create.CreateBaoCaoService;
import com.example.suco.service.suco.baocao.user.workflow.gui.duplicate.DuplicateBaoCaoService;
import com.example.suco.service.suco.baocao.user.workflow.gui.enrich.BaoCaoEnrichService;
import com.example.suco.service.suco.baocao.user.workflow.gui.response.BaoCaoResponseFactory;
import com.example.suco.service.suco.baocao.user.workflow.gui.reward.BaoCaoRewardService;

import com.example.suco.repository.suco.baocao.BaoCaoSuCoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    private SuCoResponseBuilder suCoResponseBuilder;

    @Transactional
    public BaoCaoResponse submitReport(
            String uid,
            BaoCaoRequest dto,
            String base64FullData
    ) {

        BaoCaoSuCo report =
                createBaoCaoService.create(uid, dto);

        AiVerifyResult ai =
                baoCaoAiService.verify(
                        report,
                        base64FullData
                );

        if (!ai.isValid()) {
            return baoCaoResponseFactory.reject(ai);
        }

        BaoCaoResponse duplicateResponse =
                duplicateBaoCaoService.process(
                        uid,
                        report
                );

        if (duplicateResponse != null) {
            return duplicateResponse;
        }

        BaoCaoSuCo savedReport =
                baoCaoEnrichService.enrichAndSave(
                        report,
                        base64FullData
                );

        baoCaoRewardService.rewardNewReport(
                uid,
                savedReport
        );

        return baoCaoResponseFactory.success(
                savedReport
        );
    }

    public List<SuCoMapDto> getMyReports(String uid) {

        return reportRepository.findByReporterUid(uid)
                .stream()
                .map(suCoResponseBuilder::buildSuCoDto)
                .toList();
    }

    public List<SuCoMapDto> getPendingReportsForAdmin() {

        return reportRepository.findPendingReportsForAdmin()
                .stream()
                .map(suCoResponseBuilder::buildSuCoDto)
                .toList();
    }
}
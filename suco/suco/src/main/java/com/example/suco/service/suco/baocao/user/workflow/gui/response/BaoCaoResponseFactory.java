package com.example.suco.service.suco.baocao.user.workflow.gui.response;

import com.example.suco.dto.suco.baocao.user.response.BaoCaoResponse;
import com.example.suco.model.BaoCaoSuCo;

import com.example.suco.service.AiVerifyResult;

import org.springframework.stereotype.Component;

@Component
public class BaoCaoResponseFactory {

    public BaoCaoResponse success(
            BaoCaoSuCo report
    ) {

        return new BaoCaoResponse(
                "SUCCESS",
                "OK",
                report.getDoTinCay()
        );
    }

    public BaoCaoResponse reject(
            AiVerifyResult ai
    ) {

        return new BaoCaoResponse(
                "REJECTED",
                ai.getReason(),
                ai.getConfidence()
        );
    }

    public BaoCaoResponse duplicate(
            String message,
            Integer confidence
    ) {

        return new BaoCaoResponse(
                "DUPLICATE",
                message,
                confidence
        );
    }
}
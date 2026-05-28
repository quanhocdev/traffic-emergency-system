package com.example.suco.service.suco.baocao.user.workflow.gui.response;

import com.example.suco.dto.suco.baocao.ai.AiResponse;
import com.example.suco.dto.suco.baocao.ai.AiVerifyResult;
import com.example.suco.model.BaoCaoSuCo;

import org.springframework.stereotype.Component;

@Component
public class BaoCaoResponseFactory {

    public AiResponse success(
            BaoCaoSuCo report
    ) {

        return new AiResponse(
                "SUCCESS",
                "OK",
                report.getDoTinCay()
        );
    }

    public AiResponse reject(
            AiVerifyResult ai
    ) {

        return new AiResponse(
                "REJECTED",
                ai.getReason(),
                ai.getConfidence()
        );
    }

    public AiResponse duplicate(
            String message,
            Integer confidence
    ) {

        return new AiResponse(
                "DUPLICATE",
                message,
                confidence
        );
    }
}
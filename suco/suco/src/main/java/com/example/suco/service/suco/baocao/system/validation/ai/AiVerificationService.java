package com.example.suco.service.suco.baocao.system.validation.ai;

import com.example.suco.dto.suco.baocao.ai.AiVerifyResult;

public interface AiVerificationService {

    AiVerifyResult verifyImage(
            String base64,
            String incidentType
    );
}
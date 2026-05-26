package com.example.suco.dto.suco.baocao.system;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AiVerifyResult {

    @JsonProperty("isValid")
    private boolean valid;

    private Integer confidence;

    private String reason;

    public AiVerifyResult() {
    }

    public AiVerifyResult(
            boolean valid,
            Integer confidence,
            String reason
    ) {
        this.valid = valid;
        this.confidence = confidence;
        this.reason = reason;
    }

    // Getter
    public boolean isValid() {
        return valid;
    }

    public Integer getConfidence() {
        return confidence;
    }

    public String getReason() {
        return reason;
    }

    // Setter
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public void setConfidence(Integer confidence) {
        this.confidence = confidence;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
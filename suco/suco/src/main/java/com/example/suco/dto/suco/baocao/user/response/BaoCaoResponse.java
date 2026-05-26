package com.example.suco.dto.suco.baocao.user.response;

public class BaoCaoResponse {

    private String code;
    private String message;
    private Integer confidence;

    public BaoCaoResponse() {}

    public BaoCaoResponse(String code, String message, Integer confidence) {
        this.code = code;
        this.message = message;
        this.confidence = confidence;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Integer getConfidence() {
        return confidence;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setConfidence(Integer confidence) {
        this.confidence = confidence;
    }
}
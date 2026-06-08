package com.example.suco.model.enums;

public enum TrangThaiDuyet {

    PENDING("Chờ duyệt"),
    AI_APPROVED("AI duyệt"),
    VERIFIED("Đã duyệt"),
    REJECTED("Từ chối");

    private final String label;

    TrangThaiDuyet(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
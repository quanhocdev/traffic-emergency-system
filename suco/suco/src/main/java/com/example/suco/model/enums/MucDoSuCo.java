package com.example.suco.model.enums;

public enum MucDoSuCo {

    NONE("NONE", "Không có"),
    LOW("LOW", "Thấp"),
    MEDIUM("MEDIUM", "Trung bình"),
    HIGH("HIGH", "Cao");

    private final String code;
    private final String label;

    MucDoSuCo(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
}

package com.example.suco.model.enums;

public enum TrangThaiXuLy {

    CHO_XU_LY("Chờ xử lý"),
    DANG_XU_LY("Đang xử lý"),
    HOAN_THANH("Hoàn thành"),
    HUY_BO("Hủy bỏ");

    private final String label;

    TrangThaiXuLy(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

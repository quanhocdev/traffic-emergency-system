package com.example.suco.model.enums;

public enum TrangThaiXuLy {

    // AI đã duyệt + đã gán trụ sở
    DA_TIEP_NHAN("Đã tiếp nhận"),

    // Trụ sở đã nhận task nhưng chưa làm
    CHO_XU_LY("Chờ xử lý"),

    // Đang xử lý thực tế
    DANG_XU_LY("Đang xử lý"),

    // Hoàn thành
    HOAN_THANH("Hoàn thành"),

    // User hủy khi chưa xử lý hoặc chưa bắt đầu
    HUY_BO("Hủy bỏ");

    private final String label;

    TrangThaiXuLy(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    // =========================
    // Helper rules (khuyên dùng)
    // =========================

    public boolean canBeCancelledByUser() {
        return this == DA_TIEP_NHAN || this == CHO_XU_LY;
    }

    public boolean isFinalState() {
        return this == HOAN_THANH || this == HUY_BO;
    }
    public boolean canTransitionTo(TrangThaiXuLy next) {
    return switch (this) {

        case DA_TIEP_NHAN -> next == CHO_XU_LY || next == HUY_BO;

        case CHO_XU_LY -> next == DANG_XU_LY || next == HUY_BO;

        case DANG_XU_LY -> next == HOAN_THANH;

        case HOAN_THANH, HUY_BO -> false;
    };
}
}
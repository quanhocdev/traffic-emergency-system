package com.example.suco.model.enums;

public enum TrangThaiXuLy {

    CHO_ADMIN("Chờ Admin xử lý"),
    DA_TIEP_NHAN("Đã tiếp nhận"),

    DANG_DI_CHUYEN("Đang di chuyển"),

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

    // =========================
    // Helper rules
    // =========================

    public boolean canBeCancelledByUser() {
        // Cập nhật lại biến ở đây
        return this == DA_TIEP_NHAN || this == DANG_DI_CHUYEN;
    }

    public boolean isFinalState() {
        return this == HOAN_THANH || this == HUY_BO;
    }

public boolean canTransitionTo(TrangThaiXuLy next) {
    return switch (this) {
        case CHO_ADMIN -> next == DA_TIEP_NHAN || next == HUY_BO;
        case DA_TIEP_NHAN -> next == DANG_DI_CHUYEN || next == HUY_BO;
        case DANG_DI_CHUYEN -> next == DANG_XU_LY || next == HUY_BO;
        case DANG_XU_LY -> next == HOAN_THANH;
        case HOAN_THANH, HUY_BO -> false;
    };
}
}
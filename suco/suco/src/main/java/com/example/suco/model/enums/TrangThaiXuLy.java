package com.example.suco.model.enums;

public enum TrangThaiXuLy {

    // AI đã duyệt + đã gán trụ sở
    DA_TIEP_NHAN("Đã tiếp nhận"),

    // Đã đổi từ CHO_XU_LY sang DANG_DI_CHUYEN
    DANG_DI_CHUYEN("Đang di chuyển"),

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
        // 1. Từ Đã tiếp nhận -> Chuyển sang Đang di chuyển (khi bắt đầu đi) hoặc Hủy bỏ
        case DA_TIEP_NHAN -> next == DANG_DI_CHUYEN || next == HUY_BO;

        // 2. Từ Đang di chuyển -> Đến nơi và chuyển thành Đang xử lý hoặc Hủy bỏ
        case DANG_DI_CHUYEN -> next == DANG_XU_LY || next == HUY_BO;

        // 3. Từ Đang xử lý -> Hoàn thành
        case DANG_XU_LY -> next == HOAN_THANH;

        // Các trạng thái cuối
        case HOAN_THANH, HUY_BO -> false;
    };
}
}
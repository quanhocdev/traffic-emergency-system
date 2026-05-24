package com.example.suco.model.enums;

public enum TrangThaiHoatDongTruSo {

    /**
     * Hoạt động bình thường
     * được nhận tín hiệu
     */
    SAN_SANG,

    /**
     * Đang bận nhưng vẫn có thể nhận
     */
    DANG_BAN,

    /**
     * Quá tải
     * tạm thời không nhận thêm
     */
    QUA_TAI,

    /**
     * Chủ động tạm ngưng tiếp nhận
     */
    TAM_NGUNG_NHAN,

    /**
     * Offline / mất kết nối
     */
    NGOAI_TUYEN
}
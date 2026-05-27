package com.example.suco.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "thanh_toan_hoa_don")
public class ThanhToanHoaDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========= LIÊN KẾT HÓA ĐƠN =========

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hoa_don_id")
    private HoaDon hoaDon;

    // ========= THÔNG TIN THANH TOÁN =========

    @Column(name = "phuong_thuc_thanh_toan")
    private String phuongThucThanhToan;
    // CASH, MOMO, VNPAY

    @Column(name = "ma_giao_dich")
    private String maGiaoDich;

    @Column(name = "trang_thai")
    private String trangThai;
    // PENDING, SUCCESS, FAILED

    // ========= SỐ TIỀN =========

    @Column(name = "thanh_tien", precision = 12, scale = 2)
    private BigDecimal thanhTien; // SỐ TIỀN GỐC (Trước khi giảm giá)

    @Column(name = "qua_id")
    private Long quaId;

    @Column(name = "so_tien_giam", precision = 12, scale = 2)
    private BigDecimal soTienGiam;

    @Column(name = "tong_thanh_toan", precision = 12, scale = 2)
    private BigDecimal tongThanhToan;

    // ========= THỜI GIAN =========

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    
    public ThanhToanHoaDon() {
        this.createdAt = LocalDateTime.now();
    }

    // ===== Getter / Setter =====

    public Long getId() {
        return id;
    }

    public HoaDon getHoaDon() {
        return hoaDon;
    }

    public void setHoaDon(HoaDon hoaDon) {
        this.hoaDon = hoaDon;
    }

    public String getPhuongThucThanhToan() {
        return phuongThucThanhToan;
    }

    public void setPhuongThucThanhToan(String phuongThucThanhToan) {
        this.phuongThucThanhToan = phuongThucThanhToan;
    }

    public String getMaGiaoDich() {
        return maGiaoDich;
    }

    public void setMaGiaoDich(String maGiaoDich) {
        this.maGiaoDich = maGiaoDich;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public BigDecimal getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(BigDecimal thanhTien) {
        this.thanhTien = thanhTien;
    }

    public Long getQuaId() {
        return quaId;
    }

    public void setQuaId(Long quaId) {
        this.quaId = quaId;
    }

    public BigDecimal getSoTienGiam() {
        return soTienGiam;
    }

    public void setSoTienGiam(BigDecimal soTienGiam) {
        this.soTienGiam = soTienGiam;
    }

    public BigDecimal getTongThanhToan() {
        return tongThanhToan;
    }

    public void setTongThanhToan(BigDecimal tongThanhToan) {
        this.tongThanhToan = tongThanhToan;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
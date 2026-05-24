package com.example.suco.model;

import com.example.suco.model.enums.TrangThaiHoatDongTruSo;

import jakarta.persistence.*;

@Entity
@Table(name = "truso")
public class TruSo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String tenDangNhap;

    @Column(nullable = false)
    private String matKhau;

    private String tenTruSo;

    /**
     * Vị trí hoạt động
     */
    private double kinhDo;

    private double viDo;

    @Column(length = 12)
    private String geohash;

    /**
     * Trạng thái hoạt động realtime
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai_hoat_dong")
    private TrangThaiHoatDongTruSo trangThaiHoatDong =
            TrangThaiHoatDongTruSo.SAN_SANG;

    /**
     * Có đang nhận tín hiệu không
     */
    @Column(name = "dang_nhan_tin_hieu")
    private Boolean dangNhanTinHieu = true;

    /**
     * Tổng số case đang xử lý
     */
    @Column(name = "so_luong_dang_xu_ly")
    private Integer soLuongDangXuLy = 0;

    /**
     * Giới hạn xử lý tối đa
     */
    @Column(name = "gioi_han_xu_ly")
    private Integer gioiHanXuLy = 10;

    public TruSo() {}

    // =========================
    // Getter & Setter
    // =========================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getTenTruSo() {
        return tenTruSo;
    }

    public void setTenTruSo(String tenTruSo) {
        this.tenTruSo = tenTruSo;
    }

    public double getKinhDo() {
        return kinhDo;
    }

    public void setKinhDo(double kinhDo) {
        this.kinhDo = kinhDo;
    }

    public double getViDo() {
        return viDo;
    }

    public void setViDo(double viDo) {
        this.viDo = viDo;
    }

    public String getGeohash() {
        return geohash;
    }

    public void setGeohash(String geohash) {
        this.geohash = geohash;
    }

    public TrangThaiHoatDongTruSo getTrangThaiHoatDong() {
        return trangThaiHoatDong;
    }

    public void setTrangThaiHoatDong(
            TrangThaiHoatDongTruSo trangThaiHoatDong) {
        this.trangThaiHoatDong = trangThaiHoatDong;
    }

    public Boolean getDangNhanTinHieu() {
        return dangNhanTinHieu;
    }

    public void setDangNhanTinHieu(Boolean dangNhanTinHieu) {
        this.dangNhanTinHieu = dangNhanTinHieu;
    }

    public Integer getSoLuongDangXuLy() {
        return soLuongDangXuLy;
    }

    public void setSoLuongDangXuLy(Integer soLuongDangXuLy) {
        this.soLuongDangXuLy = soLuongDangXuLy;
    }

    public Integer getGioiHanXuLy() {
        return gioiHanXuLy;
    }

    public void setGioiHanXuLy(Integer gioiHanXuLy) {
        this.gioiHanXuLy = gioiHanXuLy;
    }
}
package com.example.suco.dto.sos.tinhieu;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.suco.dto.vanhanh.truso.TruSoMapDto;
import com.example.suco.dto.sos.tinhieu.UserInfoResponseDTO;

public class TheoDoiSOSDetailResponseDTO {

    private Long id;

    private Double viDo;

    private Double kinhDo;

    private String diaChi;

    private String ghiChu;

    private String hinhAnh;

    private String ghiAm;

    private String trangThai;

    private LocalDateTime createdAt;

    private TruSoMapDto truSo;

    private UserInfoResponseDTO user;

    private Long hoaDonId;

    private BigDecimal thanhTien;

    private String trangThaiHoaDon;

    // getter/setter
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Double getViDo() {
        return viDo;
    }
    public void setViDo(Double viDo) {
        this.viDo = viDo;
    }
    public Double getKinhDo() {
        return kinhDo;
    }
    public void setKinhDo(Double kinhDo) {
        this.kinhDo = kinhDo;
    }
    public String getDiaChi() {
        return diaChi;
    }
    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }
    public String getGhiChu() {
        return ghiChu;
    }
    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
    public String getHinhAnh() {
        return hinhAnh;
    }
    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }
    public String getGhiAm() {
        return ghiAm;
    }
    public void setGhiAm(String ghiAm) {
        this.ghiAm = ghiAm;
    }
    public String getTrangThai() {
        return trangThai;
    }
    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public TruSoMapDto getTruSo() {
        return truSo;
    }
    public void setTruSo(TruSoMapDto truSo) {
        this.truSo = truSo;
    }
    public UserInfoResponseDTO getUser() {
        return user;
    }
    public void setUser(UserInfoResponseDTO user) {
        this.user = user;
    }
    public Long getHoaDonId() {
        return hoaDonId;
    }
    public void setHoaDonId(Long hoaDonId) {
        this.hoaDonId = hoaDonId;
    }
    public BigDecimal getThanhTien() {
        return thanhTien;
    }
    public void setThanhTien(BigDecimal thanhTien) {
        this.thanhTien = thanhTien;
    }
    public String getTrangThaiHoaDon() {
        return trangThaiHoaDon;
    }
    public void setTrangThaiHoaDon(String trangThaiHoaDon) {
        this.trangThaiHoaDon = trangThaiHoaDon;
    }

}
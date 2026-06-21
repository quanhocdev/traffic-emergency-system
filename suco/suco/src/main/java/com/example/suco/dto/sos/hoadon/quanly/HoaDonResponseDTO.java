package com.example.suco.dto.sos.hoadon.quanly;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class HoaDonResponseDTO {

    private Long id;
    private Long sosId;
    private Long trusoId;
    private String userId;
    private String noiDungXuLy;
    private BigDecimal thanhTien;
    private LocalDateTime createdAt;
    private String trangThai;
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getSosId() {
        return sosId;
    }
    public void setSosId(Long sosId) {
        this.sosId = sosId;
    }
    public Long getTrusoId() {
        return trusoId;
    }
    public void setTrusoId(Long trusoId) {
        this.trusoId = trusoId;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getNoiDungXuLy() {
        return noiDungXuLy;
    }
    public void setNoiDungXuLy(String noiDungXuLy) {
        this.noiDungXuLy = noiDungXuLy;
    }
    public BigDecimal getThanhTien() {
        return thanhTien;
    }
    public void setThanhTien(BigDecimal thanhTien) {
        this.thanhTien = thanhTien;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public String getTrangThai() {
        return trangThai;
    }
    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}
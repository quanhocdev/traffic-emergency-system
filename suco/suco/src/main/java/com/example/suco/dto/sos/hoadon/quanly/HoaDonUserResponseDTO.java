package com.example.suco.dto.sos.hoadon.quanly;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.suco.dto.info.truso.TruSoMiniDTO;
import com.example.suco.dto.info.user.UserMiniDTO;

public class HoaDonUserResponseDTO {

    private Long id;

    private TruSoMiniDTO truSo;

    private UserMiniDTO user;

    private String noiDungXuLy;

    private BigDecimal thanhTien;

    private String trangThai;

    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public TruSoMiniDTO getTruSo() {
        return truSo;
    }
    
    
public void setTruSo(TruSoMiniDTO truSo) {
    this.truSo = truSo;
}


    public UserMiniDTO getUser() {
    return user;
}

public void setUser(UserMiniDTO user) {
    this.user = user;
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
}
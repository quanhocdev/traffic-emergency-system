package com.example.suco.dto.sos.tinhieu;
import java.time.LocalDateTime;

public class TheoDoiSOSItemResponseDTO {

    private Long id;

    private String hinhAnh;

    private String trangThai;

    private LocalDateTime createdAt;

    private String tenTruSo;

    // getter/setter
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getHinhAnh() {
        return hinhAnh;
    }
    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
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
    public String getTenTruSo() {
        return tenTruSo;
    }
    public void setTenTruSo(String tenTruSo) {
        this.tenTruSo = tenTruSo;
    }
    
}
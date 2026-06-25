package com.example.suco.dto.vanhanh.truso;

public class TruSoResponseDTO {

    private Long id;
    private String tenDangNhap;
    private String tenTruSo;
    private Double kinhDo;
    private Double viDo;
    private String diaChi;

    // getter setter
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

    public String getTenTruSo() {
        return tenTruSo;
    }

    public void setTenTruSo(String tenTruSo) {
        this.tenTruSo = tenTruSo;
    }

    public Double getKinhDo() {
        return kinhDo;
    }

    public void setKinhDo(Double kinhDo) {
        this.kinhDo = kinhDo;
    }

    public Double getViDo() {
        return viDo;
    }

    public void setViDo(Double viDo) {
        this.viDo = viDo;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }
}
package com.example.suco.dto.vanhanh.truso;

public class TruSoMapDto {
    private Long id;
    private String tenTruSo;
    private double kinhDo;
    private double viDo;
    private String diaChi;

    public TruSoMapDto(Long id, String tenTruSo, double kinhDo, double viDo, String diaChi) {
        this.id = id;
        this.tenTruSo = tenTruSo;
        this.kinhDo = kinhDo;
        this.viDo = viDo;
        this.diaChi = diaChi;
    }
    // Getters và Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
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
    public String getDiaChi() {
        return diaChi;
    }
    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }
}
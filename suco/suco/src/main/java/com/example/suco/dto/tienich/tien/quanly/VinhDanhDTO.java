package com.example.suco.dto.tienich.tien.quanly;

public class VinhDanhDTO {
    private String tenHienThi;
    private Long giaTri;

    public VinhDanhDTO() {}

    public VinhDanhDTO(String tenHienThi, Long giaTri) {
        this.tenHienThi = tenHienThi;
        this.giaTri = giaTri;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }
    public void setTenHienThi(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }
    public Long getGiaTri() {
        return giaTri;
    }
    public void setGiaTri(Long giaTri) {
        this.giaTri = giaTri;
    }
}

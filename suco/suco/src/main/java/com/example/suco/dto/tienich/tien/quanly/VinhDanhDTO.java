package com.example.suco.dto.tienich.tien.quanly;

public class VinhDanhDTO {

    private String tenHienThi;

    // tổng đóng góp trong khoảng thời gian lọc
    private Long tongDongGop;


    public VinhDanhDTO() {
    }


    public VinhDanhDTO(String tenHienThi, Long tongDongGop) {
        this.tenHienThi = tenHienThi;
        this.tongDongGop = tongDongGop;
    }


    public String getTenHienThi() {
        return tenHienThi;
    }

    public void setTenHienThi(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }


    public Long getTongDongGop() {
        return tongDongGop;
    }

    public void setTongDongGop(Long tongDongGop) {
        this.tongDongGop = tongDongGop;
    }
}
package com.example.suco.dto.sos.payment.hoadon.request;

public class HoaDonRequestDTO {

    private Long sosId;
    private String tenSos;
    private String noiDungXuLy;
    private Double giaThuCong;

    public Long getSosId() {
        return sosId;
    }
    public void setSosId(Long sosId) {
        this.sosId = sosId;
    }
    public String getTenSos() {
        return tenSos;
    }
    public void setTenSos(String tenSos) {
        this.tenSos = tenSos;
    }
    public String getNoiDungXuLy() {
        return noiDungXuLy;
    }
    public void setNoiDungXuLy(String noiDungXuLy) {
        this.noiDungXuLy = noiDungXuLy;
    }
    public Double getGiaThuCong() {
        return giaThuCong;
    }
    public void setGiaThuCong(Double giaThuCong) {
        this.giaThuCong = giaThuCong;
    }
}
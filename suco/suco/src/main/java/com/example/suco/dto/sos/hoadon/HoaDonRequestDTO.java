package com.example.suco.dto.sos.hoadon;

public class HoaDonRequestDTO {

    private Long sosId;
    private String noiDungXuLy;
    private Double giaThuCong;

    public Long getSosId() {
        return sosId;
    }
    public void setSosId(Long sosId) {
        this.sosId = sosId;
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
package com.example.suco.dto.tienich.tien.quydoi;

public class DoiTienRequestDTO {

    private int soDiemDoi;
    private String loaiDoi;

    public DoiTienRequestDTO() {
    }

    public int getSoDiemDoi() {
        return soDiemDoi;
    }

    public void setSoDiemDoi(int soDiemDoi) {
        this.soDiemDoi = soDiemDoi;
    }

    public String getLoaiDoi() {
        return loaiDoi;
    }

    public void setLoaiDoi(String loaiDoi) {
        this.loaiDoi = loaiDoi;
    }
}

package com.example.suco.dto.tienich.tien.quanly;

import java.util.List;

public class ThongKeQuyDTO {

    private long tongGiaTri;
    private List<VinhDanhDTO> bangVinhDanh;

    public ThongKeQuyDTO() {
    }

    public ThongKeQuyDTO(long tongGiaTri, List<VinhDanhDTO> bangVinhDanh) {
        this.tongGiaTri = tongGiaTri;
        this.bangVinhDanh = bangVinhDanh;
    }

    public long getTongGiaTri() {
        return tongGiaTri;
    }

    public void setTongGiaTri(long tongGiaTri) {
        this.tongGiaTri = tongGiaTri;
    }

    public List<VinhDanhDTO> getBangVinhDanh() {
        return bangVinhDanh;
    }

    public void setBangVinhDanh(List<VinhDanhDTO> bangVinhDanh) {
        this.bangVinhDanh = bangVinhDanh;
    }
}
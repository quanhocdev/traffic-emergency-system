package com.example.suco.dto.tienich.tien.quydoi;

import java.time.LocalDateTime;

public class DoiTienResponseDTO {

    private Long id;

    private int soDiemDoi;

    private Long giaTri;

    private LocalDateTime ngayDoi;


    public DoiTienResponseDTO() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public int getSoDiemDoi() {
        return soDiemDoi;
    }

    public void setSoDiemDoi(int soDiemDoi) {
        this.soDiemDoi = soDiemDoi;
    }


    public Long getGiaTri() {
        return giaTri;
    }

    public void setGiaTri(Long giaTri) {
        this.giaTri = giaTri;
    }


    public LocalDateTime getNgayDoi() {
        return ngayDoi;
    }

    public void setNgayDoi(LocalDateTime ngayDoi) {
        this.ngayDoi = ngayDoi;
    }
}
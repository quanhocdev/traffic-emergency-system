package com.example.suco.dto.tienich.tien.quyengop;

import java.time.LocalDateTime;

public class QuyenGopResponseDTO {

    private Long id;

    private int soDiemQuyenGop;

    private Long giaTri;

    private String noiDung;

    private LocalDateTime ngayQuyenGop;


    public QuyenGopResponseDTO() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public int getSoDiemQuyenGop() {
        return soDiemQuyenGop;
    }

    public void setSoDiemQuyenGop(int soDiemQuyenGop) {
        this.soDiemQuyenGop = soDiemQuyenGop;
    }


    public Long getGiaTri() {
        return giaTri;
    }

    public void setGiaTri(Long giaTri) {
        this.giaTri = giaTri;
    }


    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }


    public LocalDateTime getNgayQuyenGop() {
        return ngayQuyenGop;
    }

    public void setNgayQuyenGop(LocalDateTime ngayQuyenGop) {
        this.ngayQuyenGop = ngayQuyenGop;
    }
}
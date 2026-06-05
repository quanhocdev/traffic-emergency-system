package com.example.suco.dto.sos.tinhieu;
import com.example.suco.dto.sos.hoadon.quanly.TruSoMiniDTO;
import com.example.suco.model.TruSo;

public class GuiTinHieuResponseDTO {

    private TinHieuSOSResponseDTO sosData;

    private TruSoMiniDTO truSoGanNhat;

    public GuiTinHieuResponseDTO() {}

    public GuiTinHieuResponseDTO(TinHieuSOSResponseDTO sosData, TruSoMiniDTO truSoGanNhat) {
        this.sosData = sosData;
        this.truSoGanNhat = truSoGanNhat;
    }

    // getter/setter
    public TinHieuSOSResponseDTO getSosData() {
        return sosData;
    }
    public void setSosData(TinHieuSOSResponseDTO sosData) {
        this.sosData = sosData;
    }
    public TruSoMiniDTO getTruSoGanNhat() {
        return truSoGanNhat;
    }
    public void setTruSoGanNhat(TruSoMiniDTO truSoGanNhat) {
        this.truSoGanNhat = truSoGanNhat;
    }

}

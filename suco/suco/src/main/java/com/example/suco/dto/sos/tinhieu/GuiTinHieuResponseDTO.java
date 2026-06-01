package com.example.suco.dto.sos.tinhieu;
import com.example.suco.model.TruSo;

public class GuiTinHieuResponseDTO {

    private TinHieuSOSResponseDTO sosData;

    private TruSo truSoGanNhat;

    public GuiTinHieuResponseDTO() {}

    public GuiTinHieuResponseDTO(TinHieuSOSResponseDTO sosData, TruSo truSoGanNhat) {
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
    public TruSo getTruSoGanNhat() {
        return truSoGanNhat;
    }
    public void setTruSoGanNhat(TruSo truSoGanNhat) {
        this.truSoGanNhat = truSoGanNhat;
    }

}

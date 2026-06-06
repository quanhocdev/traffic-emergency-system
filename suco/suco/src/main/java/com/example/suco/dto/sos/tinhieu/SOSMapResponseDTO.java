package com.example.suco.dto.sos.tinhieu;

import com.example.suco.dto.sos.hoadon.quanly.TruSoMiniDTO;

public class SOSMapResponseDTO {


    private Long id;

    private Double viDo;
    private Double kinhDo;

    private TruSoMiniDTO truSo;

    private String trangThai;

    public SOSMapResponseDTO() {}
    public SOSMapResponseDTO(Long id, Double viDo, Double kinhDo, TruSoMiniDTO truSo, String trangThai) {
        this.id = id;
        this.viDo = viDo;
        this.kinhDo = kinhDo;
        this.truSo = truSo;
        this.trangThai = trangThai;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Double getViDo() { return viDo; }
    public void setViDo(Double viDo) { this.viDo = viDo; }
    public Double getKinhDo() { return kinhDo; }
    public void setKinhDo(Double kinhDo) { this.kinhDo = kinhDo; }
    public TruSoMiniDTO getTruSo() { return truSo; }
    public void setTruSo(TruSoMiniDTO truSo) { this.truSo = truSo; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    
}

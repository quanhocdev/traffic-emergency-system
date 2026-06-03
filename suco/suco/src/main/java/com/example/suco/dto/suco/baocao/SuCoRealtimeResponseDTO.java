package com.example.suco.dto.suco.baocao;

public class SuCoRealtimeResponseDTO {

    private Long id;
    private Double viDo;
    private Double kinhDo;
    private String trangThaiXuLy;
    private String mucDoNghiemTrong;
    private Long truSoId;

    public SuCoRealtimeResponseDTO() {}

    public SuCoRealtimeResponseDTO(Long id, Double viDo, Double kinhDo,
                                   String trangThaiXuLy,
                                   String mucDoNghiemTrong,
                                   Long truSoId) {
        this.id = id;
        this.viDo = viDo;
        this.kinhDo = kinhDo;
        this.trangThaiXuLy = trangThaiXuLy;
        this.mucDoNghiemTrong = mucDoNghiemTrong;
        this.truSoId = truSoId;
    }

    public Long getId() { return id; }
    public Double getViDo() { return viDo; }
    public Double getKinhDo() { return kinhDo; }
    public String getTrangThaiXuLy() { return trangThaiXuLy; }
    public String getMucDoNghiemTrong() { return mucDoNghiemTrong; }
    public Long getTruSoId() { return truSoId; }

    public void setId(Long id) { this.id = id; }
    public void setViDo(Double viDo) { this.viDo = viDo; }
    public void setKinhDo(Double kinhDo) { this.kinhDo = kinhDo; }
    public void setTrangThaiXuLy(String trangThaiXuLy) { this.trangThaiXuLy = trangThaiXuLy; }
    public void setMucDoNghiemTrong(String mucDoNghiemTrong) { this.mucDoNghiemTrong = mucDoNghiemTrong; }
    public void setTruSoId(Long truSoId) { this.truSoId = truSoId; }
}
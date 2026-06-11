package com.example.suco.dto.suco.baocao;

public class SuCoMapResponseDTO {
    private Long id;
    private Double viDo;
    private Double kinhDo;
    private String iconUrl;
    private String trangThaiXuLy;
    private String mucDoSuCo;
    private Long truSoId;

    public SuCoMapResponseDTO() {}

    public SuCoMapResponseDTO(Long id, Double viDo, Double kinhDo,
                              String iconUrl,
                              String trangThaiXuLy,
                              String mucDoSuCo,
                                   Long truSoId) {
        this.id = id;
        this.viDo = viDo;
        this.kinhDo = kinhDo;
        this.iconUrl = iconUrl;
        this.trangThaiXuLy = trangThaiXuLy;
        this.mucDoSuCo = mucDoSuCo;
        this.truSoId = truSoId;
    }

    public Long getId() { return id; }
    public Double getViDo() { return viDo; }
    public Double getKinhDo() { return kinhDo; }
    public String getIconUrl() { return iconUrl; }

    public String getTrangThaiXuLy() { return trangThaiXuLy; }
    public String getMucDoSuCo() { return mucDoSuCo; }
    public Long getTruSoId() { return truSoId; }

    public void setId(Long id) { this.id = id; }
    public void setViDo(Double viDo) { this.viDo = viDo; }
    public void setKinhDo(Double kinhDo) { this.kinhDo = kinhDo; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
    public void setTrangThaiXuLy(String trangThaiXuLy) { this.trangThaiXuLy = trangThaiXuLy; }
    public void setMucDoSuCo(String mucDoSuCo) { this.mucDoSuCo = mucDoSuCo; }
    public void setTruSoId(Long truSoId) { this.truSoId = truSoId; }
}
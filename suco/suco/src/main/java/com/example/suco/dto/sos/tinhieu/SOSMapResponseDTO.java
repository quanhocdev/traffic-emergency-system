package com.example.suco.dto.sos.tinhieu;

public class SOSMapResponseDTO {

    private Long id;
    private Double viDo;
    private Double kinhDo;
    private Long truSoId;
    public SOSMapResponseDTO() {}
    public SOSMapResponseDTO(Long id, Double viDo, Double kinhDo, Long truSoId) {
        this.id = id;
        this.viDo = viDo;
        this.kinhDo = kinhDo;
        this.truSoId = truSoId;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Double getViDo() { return viDo; }
    public void setViDo(Double viDo) { this.viDo = viDo; }
    public Double getKinhDo() { return kinhDo; }
    public void setKinhDo(Double kinhDo) { this.kinhDo = kinhDo; }
    public Long getTruSoId() { return truSoId; }
    public void setTruSoId(Long truSoId) { this.truSoId = truSoId; }

    
}

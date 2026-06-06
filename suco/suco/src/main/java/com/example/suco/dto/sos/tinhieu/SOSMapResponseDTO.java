package com.example.suco.dto.sos.tinhieu;

public class SOSMapResponseDTO {


    private Long id;

    private Double viDo;
    private Double kinhDo;

    private Long truSoId;

    private String trangThai;

    private String diaChi;

    public SOSMapResponseDTO() {}
    public SOSMapResponseDTO(Long id, Double viDo, Double kinhDo, Long truSoId, String trangThai, String diaChi) {
        this.id = id;
        this.viDo = viDo;
        this.kinhDo = kinhDo;
        this.truSoId = truSoId;
        this.trangThai = trangThai;
        this.diaChi = diaChi;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Double getViDo() { return viDo; }
    public void setViDo(Double viDo) { this.viDo = viDo; }
    public Double getKinhDo() { return kinhDo; }
    public void setKinhDo(Double kinhDo) { this.kinhDo = kinhDo; }
    public Long getTruSoId() { return truSoId; }
    public void setTruSoId(Long truSoId) { this.truSoId = truSoId; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    

    
}

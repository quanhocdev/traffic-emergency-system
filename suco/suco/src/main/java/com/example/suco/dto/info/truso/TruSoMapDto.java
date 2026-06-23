package com.example.suco.dto.info.truso;

public class TruSoMapDto extends TruSoMiniDTO {

    private double kinhDo;
    private double viDo;
    private String diaChi;

    public TruSoMapDto() {
        super();
    }

    public TruSoMapDto(Long id, String tenTruSo, double kinhDo, double viDo, String diaChi) {
        super(id, tenTruSo); 
        this.kinhDo = kinhDo;
        this.viDo = viDo;
        this.diaChi = diaChi;
    }

    public double getKinhDo() { return kinhDo; }
    public void setKinhDo(double kinhDo) { this.kinhDo = kinhDo; }

    public double getViDo() { return viDo; }
    public void setViDo(double viDo) { this.viDo = viDo; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
}
package com.example.suco.dto.suco.baocao;

public class SuCoMapResponseDTO {

    private Long id;
    private Double viDo;
    private Double kinhDo;
    private String tenLoai;
    private String iconUrl;
    private String trangThaiXuLy;
    private String mucDoNghiemTrong;

    public SuCoMapResponseDTO() {}

    public SuCoMapResponseDTO(Long id, Double viDo, Double kinhDo,
                              String tenLoai, String iconUrl,
                              String trangThaiXuLy, String mucDoNghiemTrong) {
        this.id = id;
        this.viDo = viDo;
        this.kinhDo = kinhDo;
        this.tenLoai = tenLoai;
        this.iconUrl = iconUrl;
        this.trangThaiXuLy = trangThaiXuLy;
        this.mucDoNghiemTrong = mucDoNghiemTrong;
    }

    public Long getId() { return id; }
    public Double getViDo() { return viDo; }
    public Double getKinhDo() { return kinhDo; }
    public String getTenLoai() { return tenLoai; }
    public String getIconUrl() { return iconUrl; }
    public String getTrangThaiXuLy() { return trangThaiXuLy; }
    public String getMucDoNghiemTrong() { return mucDoNghiemTrong; }

    public void setId(Long id) { this.id = id; }
    public void setViDo(Double viDo) { this.viDo = viDo; }
    public void setKinhDo(Double kinhDo) { this.kinhDo = kinhDo; }
    public void setTenLoai(String tenLoai) { this.tenLoai = tenLoai; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
    public void setTrangThaiXuLy(String trangThaiXuLy) { this.trangThaiXuLy = trangThaiXuLy; }
    public void setMucDoNghiemTrong(String mucDoNghiemTrong) { this.mucDoNghiemTrong = mucDoNghiemTrong; }
}
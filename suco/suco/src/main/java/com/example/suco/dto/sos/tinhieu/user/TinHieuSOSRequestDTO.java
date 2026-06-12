package com.example.suco.dto.sos.tinhieu.user;

public class TinHieuSOSRequestDTO {
    private double viDo;
    private double kinhDo;
    private String ghiAmBase64;
    private String hinhAnhBase64;
    private String ghiChu;
    
    public TinHieuSOSRequestDTO() {}

    public double getViDo() { return viDo; }
    public void setViDo(double viDo) { this.viDo = viDo; }

    public double getKinhDo() { return kinhDo; }
    public void setKinhDo(double kinhDo) { this.kinhDo = kinhDo; }

    public String getGhiAmBase64() { return ghiAmBase64; }
    public void setGhiAmBase64(String ghiAmBase64) { this.ghiAmBase64 = ghiAmBase64; }

    public String getHinhAnhBase64() { return hinhAnhBase64; }
    public void setHinhAnhBase64(String hinhAnhBase64) { this.hinhAnhBase64 = hinhAnhBase64; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

}
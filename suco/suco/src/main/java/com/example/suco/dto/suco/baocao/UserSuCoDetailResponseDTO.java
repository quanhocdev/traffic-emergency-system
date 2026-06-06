package com.example.suco.dto.suco.baocao;

import java.time.LocalDateTime;

public class UserSuCoDetailResponseDTO {

    private Long id;
    private Double viDo;
    private Double kinhDo;
    private String moTa;

    private String tenLoai;
    private String iconUrl;

    private String trangThaiDuyet;
    private String trangThaiXuLy;

    private String mucDoNghiemTrong;
    private String hinhAnhUrl;

    private Integer doTinCay;

    private String tenNguoiBao;

    private String diaChi;
    private LocalDateTime thoiGianTao;

    public UserSuCoDetailResponseDTO() {}

    // GETTERS
    public Long getId() { return id; }
    public Double getViDo() { return viDo; }
    public Double getKinhDo() { return kinhDo; }
    public String getMoTa() { return moTa; }
    public String getTenLoai() { return tenLoai; }
    public String getIconUrl() { return iconUrl; }
    public String getTrangThaiDuyet() { return trangThaiDuyet; }
    public String getTrangThaiXuLy() { return trangThaiXuLy; }
    public String getMucDoNghiemTrong() { return mucDoNghiemTrong; }
    public String getHinhAnhUrl() { return hinhAnhUrl; }
    public Integer getDoTinCay() { return doTinCay; }
    public String getTenNguoiBao() { return tenNguoiBao; }
    public String getDiaChi() { return diaChi; }
    public LocalDateTime getThoiGianTao() { return thoiGianTao; }

    // SETTERS
    public void setId(Long id) { this.id = id; }
    public void setViDo(Double viDo) { this.viDo = viDo; }
    public void setKinhDo(Double kinhDo) { this.kinhDo = kinhDo; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public void setTenLoai(String tenLoai) { this.tenLoai = tenLoai; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
    public void setTrangThaiDuyet(String trangThaiDuyet) { this.trangThaiDuyet = trangThaiDuyet; }
    public void setTrangThaiXuLy(String trangThaiXuLy) { this.trangThaiXuLy = trangThaiXuLy; }
    public void setMucDoNghiemTrong(String mucDoNghiemTrong) { this.mucDoNghiemTrong = mucDoNghiemTrong; }
    public void setHinhAnhUrl(String hinhAnhUrl) { this.hinhAnhUrl = hinhAnhUrl; }
    public void setDoTinCay(Integer doTinCay) { this.doTinCay = doTinCay; }
    public void setTenNguoiBao(String tenNguoiBao) { this.tenNguoiBao = tenNguoiBao; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    public void setThoiGianTao(LocalDateTime thoiGianTao) { this.thoiGianTao = thoiGianTao; }
}
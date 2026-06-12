package com.example.suco.dto.suco.baocao.admin;

import com.example.suco.dto.sos.hoadon.quanly.TruSoMiniDTO;
import java.time.LocalDateTime;

public class AdminSuCoDetailResponseDTO {

    private Long id;
    private Double viDo;
    private Double kinhDo;
    private String moTa;

    private String tenLoai;
    private String iconUrl;

    private String trangThaiXuLy;

    private String mucDoSuCo;
    private String hinhAnhUrl;

    private Integer doTinCay;

    private String tenNguoiBao;
    private String reporterUid;

    private TruSoMiniDTO truSoTiepNhan;

    private String diaChi;
    private LocalDateTime thoiGianTao;

    public AdminSuCoDetailResponseDTO() {}

    // GETTERS
    public Long getId() { return id; }
    public Double getViDo() { return viDo; }
    public Double getKinhDo() { return kinhDo; }
    public String getMoTa() { return moTa; }
    public String getTenLoai() { return tenLoai; }
    public String getIconUrl() { return iconUrl; }
    public String getTrangThaiXuLy() { return trangThaiXuLy; }
    public String getMucDoSuCo() { return mucDoSuCo; }
    public String getHinhAnhUrl() { return hinhAnhUrl; }
    public Integer getDoTinCay() { return doTinCay; }
    public String getTenNguoiBao() { return tenNguoiBao; }
    public String getReporterUid() { return reporterUid; }
    public TruSoMiniDTO getTruSoTiepNhan() { return truSoTiepNhan; }
    public String getDiaChi() { return diaChi; }
    public LocalDateTime getThoiGianTao() { return thoiGianTao; }

    // SETTERS
    public void setId(Long id) { this.id = id; }
    public void setViDo(Double viDo) { this.viDo = viDo; }
    public void setKinhDo(Double kinhDo) { this.kinhDo = kinhDo; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public void setTenLoai(String tenLoai) { this.tenLoai = tenLoai; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
    public void setTrangThaiXuLy(String trangThaiXuLy) { this.trangThaiXuLy = trangThaiXuLy; }
    public void setMucDoSuCo(String mucDoSuCo) { this.mucDoSuCo = mucDoSuCo; }
    public void setHinhAnhUrl(String hinhAnhUrl) { this.hinhAnhUrl = hinhAnhUrl; }
    public void setDoTinCay(Integer doTinCay) { this.doTinCay = doTinCay; }
    public void setTenNguoiBao(String tenNguoiBao) { this.tenNguoiBao = tenNguoiBao; }
    public void setReporterUid(String reporterUid) { this.reporterUid = reporterUid; }
    public void setTruSoTiepNhan(TruSoMiniDTO truSoTiepNhan) { this.truSoTiepNhan = truSoTiepNhan; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    public void setThoiGianTao(LocalDateTime thoiGianTao) { this.thoiGianTao = thoiGianTao; }
}
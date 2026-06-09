package com.example.suco.dto.sos.tinhieu.truso;

import com.example.suco.dto.sos.hoadon.quanly.HoaDonResponseDTO;
import java.time.LocalDateTime;
import com.example.suco.dto.sos.tinhieu.UserMiniDTO;

public class TruSoSOSDetailResponseDTO {

    private Long id;
    private Double viDo;
    private Double kinhDo;
    private String diaChi;
    private String ghiChu;
    private String hinhAnhUrl;
    private String ghiAmUrl;
    private LocalDateTime thoiGianTao;
    private String trangThai;
    private UserMiniDTO nguoiGui;
    
    private HoaDonResponseDTO hoaDon; 

    public TruSoSOSDetailResponseDTO() {}

    public HoaDonResponseDTO getHoaDon() {
        return hoaDon;
    }

    public void setHoaDon(HoaDonResponseDTO hoaDon) {
        this.hoaDon = hoaDon;
    }

    // --- Các Getter và Setter cũ giữ nguyên ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Double getViDo() { return viDo; }
    public void setViDo(Double viDo) { this.viDo = viDo; }
    public Double getKinhDo() { return kinhDo; }
    public void setKinhDo(Double kinhDo) { this.kinhDo = kinhDo; }
    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public String getHinhAnhUrl() { return hinhAnhUrl; }
    public void setHinhAnhUrl(String hinhAnhUrl) { this.hinhAnhUrl = hinhAnhUrl; }
    public String getGhiAmUrl() { return ghiAmUrl; }
    public void setGhiAmUrl(String ghiAmUrl) { this.ghiAmUrl = ghiAmUrl; }
    public LocalDateTime getThoiGianTao() { return thoiGianTao; }
    public void setThoiGianTao(LocalDateTime thoiGianTao) { this.thoiGianTao = thoiGianTao; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public UserMiniDTO getNguoiGui() { return nguoiGui; }
    public void setNguoiGui(UserMiniDTO nguoiGui) { this.nguoiGui = nguoiGui; }
}
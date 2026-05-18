package com.example.suco.dto.sos;

import java.time.LocalDateTime;

public class TinHieuSOSResponseDTO {
    private Long id;
    private double viDo;
    private double kinhDo;
    private String diaChi;
    private String ghiChu;
    private LocalDateTime createdAt;
    private String trangThai;

    private String userId; // giữ lại nếu cần

    private UserMiniDTO user; // ⭐ QUAN TRỌNG

    public TinHieuSOSResponseDTO() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public double getViDo() { return viDo; }
    public void setViDo(double viDo) { this.viDo = viDo; }
    public double getKinhDo() { return kinhDo; }
    public void setKinhDo(double kinhDo) { this.kinhDo = kinhDo; }
    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public UserMiniDTO getUser() { return user; }
    public void setUser(UserMiniDTO user) { this.user = user; }

}
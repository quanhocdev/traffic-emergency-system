package com.example.suco.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "hoa_don")
public class HoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sosId;
    private Long trusoId;
    private String userId;
    private String noiDungXuLy; 
    
    @Column(precision = 12, scale = 2)
    private BigDecimal thanhTien;
    
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL)
private List<ThanhToanHoaDon> thanhToans;


    public HoaDon() { this.createdAt = LocalDateTime.now(); }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSosId() { return sosId; }
    public void setSosId(Long sosId) { this.sosId = sosId; }
    public Long getTrusoId() { return trusoId; }
    public void setTrusoId(Long trusoId) { this.trusoId = trusoId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getNoiDungXuLy() { return noiDungXuLy; }
    public void setNoiDungXuLy(String noiDungXuLy) { this.noiDungXuLy = noiDungXuLy; }
    public BigDecimal getThanhTien() { return thanhTien; }
    public void setThanhTien(BigDecimal thanhTien) { this.thanhTien = thanhTien; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<ThanhToanHoaDon> getThanhToans() {
    return thanhToans;
}

public void setThanhToans(List<ThanhToanHoaDon> thanhToans) {
    this.thanhToans = thanhToans;
}

}
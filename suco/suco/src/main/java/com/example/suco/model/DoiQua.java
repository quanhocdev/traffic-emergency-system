package com.example.suco.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "doi_qua")
public class DoiQua {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    private Long quaId;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "diem_da_tru")
    private Integer diemDaTru;

    @Column(name = "ngay_doi")
    private LocalDateTime ngayDoi;

    public DoiQua() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getQuaId() {
        return quaId;
    }

    public void setQuaId(Long quaId) {
        this.quaId = quaId;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public Integer getDiemDaTru() {
        return diemDaTru;
    }

    public void setDiemDaTru(Integer diemDaTru) {
        this.diemDaTru = diemDaTru;
    }

    public LocalDateTime getNgayDoi() {
        return ngayDoi;
    }

    public void setNgayDoi(LocalDateTime ngayDoi) {
        this.ngayDoi = ngayDoi;
    }
}
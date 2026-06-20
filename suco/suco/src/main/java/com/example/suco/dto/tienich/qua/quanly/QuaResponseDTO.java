package com.example.suco.dto.tienich.qua.quanly;

import com.example.suco.model.Qua.LoaiQua;
import com.example.suco.model.Qua.TrangThai;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class QuaResponseDTO {

    private Long id; 
    private String ten;
    private LoaiQua loai;
    private String moTa;
    private Integer diem;
    private String hinhAnh;
    private Integer giaTriGiamPercent;
    private BigDecimal giaTriToiDa;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime ngayKetThuc;
    
    private TrangThai trangThai;

    public QuaResponseDTO() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }

    public LoaiQua getLoai() { return loai; }
    public void setLoai(LoaiQua loai) { this.loai = loai; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public Integer getDiem() { return diem; }
    public void setDiem(Integer diem) { this.diem = diem; }

    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }

    public Integer getGiaTriGiamPercent() { return giaTriGiamPercent; }
    public void setGiaTriGiamPercent(Integer giaTriGiamPercent) { this.giaTriGiamPercent = giaTriGiamPercent; }

    public BigDecimal getGiaTriToiDa() { return giaTriToiDa; }
    public void setGiaTriToiDa(BigDecimal giaTriToiDa) { this.giaTriToiDa = giaTriToiDa; }

    public LocalDateTime getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(LocalDateTime ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }

    public TrangThai getTrangThai() { return trangThai; }
    public void setTrangThai(TrangThai trangThai) { this.trangThai = trangThai; }
}
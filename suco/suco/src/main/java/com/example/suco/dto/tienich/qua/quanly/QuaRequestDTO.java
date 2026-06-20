package com.example.suco.dto.tienich.qua.quanly;

import com.example.suco.model.Qua.LoaiQua;
import com.example.suco.model.Qua.TrangThai;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class QuaRequestDTO {

    private String ten;
    private LoaiQua loai;
    private String moTa;
    private Integer diem;
    
    private MultipartFile hinhAnh; 
    
    private Integer giaTriGiamPercent; 
    private BigDecimal giaTriToiDa;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime ngayKetThuc;
    
    private TrangThai trangThai;

    public QuaRequestDTO() {}

    // Getters and Setters
    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }

    public LoaiQua getLoai() { return loai; }
    public void setLoai(LoaiQua loai) { this.loai = loai; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public Integer getDiem() { return diem; }
    public void setDiem(Integer diem) { this.diem = diem; }

    public MultipartFile getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(MultipartFile hinhAnh) { this.hinhAnh = hinhAnh; }

    public Integer getGiaTriGiamPercent() { return giaTriGiamPercent; }
    public void setGiaTriGiamPercent(Integer giaTriGiamPercent) { this.giaTriGiamPercent = giaTriGiamPercent; }

    public BigDecimal getGiaTriToiDa() { return giaTriToiDa; }
    public void setGiaTriToiDa(BigDecimal giaTriToiDa) { this.giaTriToiDa = giaTriToiDa; }

    public LocalDateTime getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(LocalDateTime ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }

    public TrangThai getTrangThai() { return trangThai; }
    public void setTrangThai(TrangThai trangThai) { this.trangThai = trangThai; }
}
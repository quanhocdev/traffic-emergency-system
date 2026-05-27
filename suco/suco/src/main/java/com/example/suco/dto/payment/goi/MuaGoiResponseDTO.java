package com.example.suco.dto.payment.goi;
import java.time.LocalDateTime;

public class MuaGoiResponseDTO {
    private String userId;
    private String tenGoi;
    private LocalDateTime ngayMua;
    private LocalDateTime ngayHetHan;
    private String trangThai;

    public MuaGoiResponseDTO(String userId, String tenGoi, LocalDateTime ngayMua, LocalDateTime ngayHetHan, String trangThai) {
        this.userId = userId;
        this.tenGoi = tenGoi;
        this.ngayMua = ngayMua;
        this.ngayHetHan = ngayHetHan;
        this.trangThai = trangThai;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTenGoi() { return tenGoi; }
    public void setTenGoi(String tenGoi) { this.tenGoi = tenGoi; }

    public LocalDateTime getNgayMua() { return ngayMua; }
    public void setNgayMua(LocalDateTime ngayMua) { this.ngayMua = ngayMua; }

    public LocalDateTime getNgayHetHan() { return ngayHetHan; }
    public void setNgayHetHan(LocalDateTime ngayHetHan) { this.ngayHetHan = ngayHetHan; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

}

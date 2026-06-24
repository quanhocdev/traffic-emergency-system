package com.example.suco.dto.sos.goi.dangky;
import java.time.LocalDateTime;

import com.example.suco.dto.sos.goi.quanly.GoiResponseDTO;

public class MuaGoiUserResponseDTO {
    private GoiResponseDTO goi;
    private LocalDateTime ngayMua;
    private LocalDateTime ngayHetHan;
    private String trangThai;

    public MuaGoiUserResponseDTO(GoiResponseDTO goi, LocalDateTime ngayMua, LocalDateTime ngayHetHan, String trangThai) {
        this.goi = goi;
        this.ngayMua = ngayMua;
        this.ngayHetHan = ngayHetHan;
        this.trangThai = trangThai;
    }

    public GoiResponseDTO getGoi() { return goi; }
    public void setGoi(GoiResponseDTO goi) { this.goi = goi; }

    public LocalDateTime getNgayMua() { return ngayMua; }
    public void setNgayMua(LocalDateTime ngayMua) { this.ngayMua = ngayMua; }

    public LocalDateTime getNgayHetHan() { return ngayHetHan; }
    public void setNgayHetHan(LocalDateTime ngayHetHan) { this.ngayHetHan = ngayHetHan; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

}

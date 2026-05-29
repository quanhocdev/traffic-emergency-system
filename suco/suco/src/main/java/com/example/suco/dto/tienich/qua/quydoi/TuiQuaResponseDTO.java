package com.example.suco.dto.tienich.qua.quydoi;
import java.time.LocalDateTime;

public class TuiQuaResponseDTO {
    private Long quaId;
    private String tenQua;
    private Integer soLuong;
    private String loai;
    private LocalDateTime ngayKetThuc;

  public TuiQuaResponseDTO() {}



    public Long getQuaId() { return quaId; }
    public void setQuaId(Long quaId) { this.quaId = quaId; }

    public String getTenQua() { return tenQua; }
    public void setTenQua(String tenQua) { this.tenQua = tenQua; }

    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }

    public String getLoai() { return loai; }
    public void setLoai(String loai) { this.loai = loai; }

    public LocalDateTime getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(LocalDateTime ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }

  
}
package com.example.suco.dto.tienich.qua.quydoi;
import java.time.LocalDateTime;

public class DoiQuaResponseDTO {

    private Long id;

    private Long quaId;

    private String tenQua;

    private Integer soLuong;

    private Integer diemDaTru;

    private String loai;

    private LocalDateTime ngayDoi;

    public DoiQuaResponseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getQuaId() { return quaId; }
    public void setQuaId(Long quaId) { this.quaId = quaId; }
    public String getTenQua() { return tenQua; }
    public void setTenQua(String tenQua) { this.tenQua = tenQua; }
    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }
    public Integer getDiemDaTru() { return diemDaTru; }
    public void setDiemDaTru(Integer diemDaTru) { this.diemDaTru = diemDaTru; }
    public String getLoai() { return loai; }
    public void setLoai(String loai) { this.loai = loai; }
    public LocalDateTime getNgayDoi() { return ngayDoi; }
    public void setNgayDoi(LocalDateTime ngayDoi) { this.ngayDoi = ngayDoi; }
    

}
package com.example.suco.dto.tienich.qua.quydoi;
import java.time.LocalDateTime;
import com.example.suco.dto.tienich.qua.quanly.QuaResponseDTO;
public class DoiQuaResponseDTO {

    private Long id;
    private QuaResponseDTO qua; 
    private Integer soLuong;
    private Integer diemDaTru;
    private LocalDateTime ngayDoi;

    public DoiQuaResponseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public QuaResponseDTO getQua() { return qua; }
    public void setQua(QuaResponseDTO qua) { this.qua = qua; }
    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }
    public Integer getDiemDaTru() { return diemDaTru; }
    public void setDiemDaTru(Integer diemDaTru) { this.diemDaTru = diemDaTru; }
    public LocalDateTime getNgayDoi() { return ngayDoi; }
    public void setNgayDoi(LocalDateTime ngayDoi) { this.ngayDoi = ngayDoi; }
    

}
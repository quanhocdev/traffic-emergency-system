package com.example.suco.dto.tienich.qua.quydoi;

public class DoiQuaRequestDTO {
    private Long quaId;
    private Integer soLuong;

    public DoiQuaRequestDTO() {}

    public Long getQuaId() { return quaId; }
    public void setQuaId(Long quaId) { this.quaId = quaId; }

    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }
  
}
package com.example.suco.dto.tienich.qua.quydoi;

import com.example.suco.dto.tienich.qua.quanly.QuaResponseDTO;

public class TuiQuaResponseDTO {
    private QuaResponseDTO qua; 
    private Integer soLuong;

    public TuiQuaResponseDTO() {}

    public QuaResponseDTO getQua() { return qua; }
    public void setQua(QuaResponseDTO qua) { this.qua = qua; }

    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }

}
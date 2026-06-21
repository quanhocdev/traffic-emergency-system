package com.example.suco.dto.sos.hoadon.quanly;

public class TruSoMiniDTO {
    private Long id;
    private String tenTruSo;


    public TruSoMiniDTO() {
    }
    public TruSoMiniDTO(Long id, String tenTruSo) {
        this.id = id;
        this.tenTruSo = tenTruSo;
    }

    // getter/setter
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getTenTruSo() {
        return tenTruSo;
    }
    public void setTenTruSo(String tenTruSo) {
        this.tenTruSo = tenTruSo;
    }
}
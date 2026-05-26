package com.example.suco.dto.suco.baocao.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BaoCaoRequest {

    @NotNull(message = "Loại sự cố không được để trống")
    private Long loaiSuCoId;

    @NotNull(message = "Kinh độ không được để trống")
    private Double kinhDo;

    @NotNull(message = "Vĩ độ không được để trống")
    private Double viDo;

    @NotBlank(message = "Mô tả không được để trống")
    private String moTa;

    @NotBlank(message = "Hình ảnh không được để trống")
    private String hinhAnhUrl;

    public BaoCaoRequest() {
    }

    public Long getLoaiSuCoId() {
        return loaiSuCoId;
    }

    public void setLoaiSuCoId(Long loaiSuCoId) {
        this.loaiSuCoId = loaiSuCoId;
    }

    public Double getKinhDo() {
        return kinhDo;
    }

    public void setKinhDo(Double kinhDo) {
        this.kinhDo = kinhDo;
    }

    public Double getViDo() {
        return viDo;
    }

    public void setViDo(Double viDo) {
        this.viDo = viDo;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getHinhAnhUrl() {
        return hinhAnhUrl;
    }

    public void setHinhAnhUrl(String hinhAnhUrl) {
        this.hinhAnhUrl = hinhAnhUrl;
    }
}
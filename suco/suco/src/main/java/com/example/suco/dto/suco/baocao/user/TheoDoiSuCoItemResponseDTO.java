package com.example.suco.dto.suco.baocao.user;
import java.time.LocalDateTime;

public class TheoDoiSuCoItemResponseDTO {

    private Long id;

    private String tenLoai;

    private String hinhAnhUrl;

    private String trangThaiXuLy;

    private LocalDateTime thoiGianTao;

    // getter/setter
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getTenLoai() {
        return tenLoai;
    }
    public void setTenLoai(String tenLoai) {
        this.tenLoai = tenLoai;
    }
    public String getHinhAnhUrl() {
        return hinhAnhUrl;
    }
    public void setHinhAnhUrl(String hinhAnhUrl) {
        this.hinhAnhUrl = hinhAnhUrl;
    }
    public String getTrangThaiXuLy() {
        return trangThaiXuLy;
    }
    public void setTrangThaiXuLy(String trangThaiXuLy) {
        this.trangThaiXuLy = trangThaiXuLy;
    }
    public LocalDateTime getThoiGianTao() {
        return thoiGianTao;
    }
    public void setThoiGianTao(LocalDateTime thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }

    

}
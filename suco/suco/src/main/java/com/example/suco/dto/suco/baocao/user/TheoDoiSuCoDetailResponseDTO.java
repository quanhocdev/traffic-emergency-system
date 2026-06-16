package com.example.suco.dto.suco.baocao.user;

import java.time.LocalDateTime;

import com.example.suco.dto.sos.tinhieu.UserInfoResponseDTO;
import com.example.suco.dto.vanhanh.truso.TruSoMapDto;

public class TheoDoiSuCoDetailResponseDTO {

    private Long id;

    private String tenLoai;

    private String moTa;

    private String hinhAnhUrl;

    private String diaChi;

    private String trangThaiXuLy;

    private Integer doTinCay;   

    private TruSoMapDto truSo;

    private UserInfoResponseDTO user;

    private String mucDoNghiemTrong;

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
    public String getDiaChi() {
        return diaChi;
    }
    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }
    public String getTrangThaiXuLy() {
        return trangThaiXuLy;
    }
    public void setTrangThaiXuLy(String trangThaiXuLy) {
        this.trangThaiXuLy = trangThaiXuLy;
    }
    public String getMucDoNghiemTrong() {
        return mucDoNghiemTrong;
    }
    public void setMucDoNghiemTrong(String mucDoNghiemTrong) {
        this.mucDoNghiemTrong = mucDoNghiemTrong;
    }
    public Integer getDoTinCay() {
        return doTinCay;
    }
    public void setDoTinCay(Integer doTinCay) {
        this.doTinCay = doTinCay;
    }
    public LocalDateTime getThoiGianTao() {
        return thoiGianTao;
    }
    public void setThoiGianTao(LocalDateTime thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }
    public TruSoMapDto getTruSo() {
        return truSo;
    }
    public void setTruSo(TruSoMapDto truSo) {
        this.truSo = truSo;
    }
    public UserInfoResponseDTO getUser() {
        return user;
    }
    public void setUser(UserInfoResponseDTO user) {
        this.user = user;
    }

}
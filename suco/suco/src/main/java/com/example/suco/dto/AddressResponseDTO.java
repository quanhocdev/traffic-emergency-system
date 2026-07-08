package com.example.suco.dto;

public class AddressResponseDTO {

    private String fullAddress;
    private String soNha;
    private String tenDuong;
    private String quan;
    private String huyen;
    private String thanhPho;
    private String tinh;

    public AddressResponseDTO() {
    }

    public AddressResponseDTO(String fullAddress,
                              String soNha,
                              String tenDuong,
                              String quan,
                              String huyen,
                              String thanhPho,
                              String tinh) {
        this.fullAddress = fullAddress;
        this.soNha = soNha;
        this.tenDuong = tenDuong;
        this.quan = quan;
        this.huyen = huyen;
        this.thanhPho = thanhPho;
        this.tinh = tinh;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public String getSoNha() {
        return soNha;
    }

    public void setSoNha(String soNha) {
        this.soNha = soNha;
    }

    public String getTenDuong() {
        return tenDuong;
    }

    public void setTenDuong(String tenDuong) {
        this.tenDuong = tenDuong;
    }

    public String getQuan() {
        return quan;
    }

    public void setQuan(String quan) {
        this.quan = quan;
    }

    public String getHuyen() {
        return huyen;
    }

    public void setHuyen(String huyen) {
        this.huyen = huyen;
    }

    public String getThanhPho() {
        return thanhPho;
    }

    public void setThanhPho(String thanhPho) {
        this.thanhPho = thanhPho;
    }

    public String getTinh() {
        return tinh;
    }

    public void setTinh(String tinh) {
        this.tinh = tinh;
    }
}
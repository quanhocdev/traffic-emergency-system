package com.example.suco.dto.vanhanh.truso;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class TruSoCreateRequestDTO {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 5, max = 20, message = "Tên đăng nhập phải từ 5 đến 20 ký tự")
    @Pattern(regexp = "^\\S+$", message = "Tên đăng nhập không được chứa khoảng trắng")
    private String tenDangNhap;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$",
        message = "Mật khẩu phải từ 8 ký tự, gồm chữ hoa, thường, số và ký tự đặc biệt"
    )
    private String matKhau;

    @NotBlank(message = "Tên trụ sở không được để trống")
    private String tenTruSo;

    @NotNull(message = "Kinh độ không được để trống")
    private Double kinhDo;

    @NotNull(message = "Vĩ độ không được để trống")
    private Double viDo;

    // --- Constructor ---
    public TruSoCreateRequestDTO() {}

    // --- Getters and Setters ---
    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }

    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }

    public String getTenTruSo() { return tenTruSo; }
    public void setTenTruSo(String tenTruSo) { this.tenTruSo = tenTruSo; }

    public Double getKinhDo() { return kinhDo; }
    public void setKinhDo(Double kinhDo) { this.kinhDo = kinhDo; }

    public Double getViDo() { return viDo; }
    public void setViDo(Double viDo) { this.viDo = viDo; }
}
package com.example.suco.dto.sos.tinhieu;

public class UserInfoResponseDTO {
    private String name;
    private boolean vip;
    private String email;

    public UserInfoResponseDTO() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isVip() { return vip; }
    public void setVip(boolean vip) { this.vip = vip; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
}

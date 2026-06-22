package com.example.suco.dto.sos.tinhieu;

public class UserInfoResponseDTO {
    private String name;
    private boolean vip;
    private String email;
    private int totalPoints;



    public UserInfoResponseDTO() {}

public UserInfoResponseDTO(String name, String email, boolean vip, int totalPoints) {
    this.name = name;
    this.email = email;
    this.vip = vip;
    this.totalPoints = totalPoints;
}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isVip() { return vip; }
    public void setVip(boolean vip) { this.vip = vip; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }
    
}

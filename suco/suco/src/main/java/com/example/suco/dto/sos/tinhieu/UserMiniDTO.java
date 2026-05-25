package com.example.suco.dto.sos.tinhieu;

public class UserMiniDTO {
    private String id;
    private String name;
    private int totalPoints;
    private boolean vip;
    private String email;
    public UserMiniDTO() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }

    public boolean isVip() { return vip; }
    public void setVip(boolean vip) { this.vip = vip; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
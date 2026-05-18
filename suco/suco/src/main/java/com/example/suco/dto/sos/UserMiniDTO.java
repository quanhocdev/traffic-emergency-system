package com.example.suco.dto.sos;

import org.checkerframework.checker.units.qual.t;

public class UserMiniDTO {
    private String id;
    private String name;
    private int totalPoints;
    private boolean vip;

    public UserMiniDTO() {}

    public UserMiniDTO(String id, String name, int totalPoints, boolean vip) {
        this.id = id;
        this.name = name;
        this.totalPoints = totalPoints;
        this.vip = vip;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }

    public boolean isVip() { return vip; }
    public void setVip(boolean vip) { this.vip = vip; }
}
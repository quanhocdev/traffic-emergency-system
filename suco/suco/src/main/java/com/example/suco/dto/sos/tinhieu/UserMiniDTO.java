package com.example.suco.dto.sos.tinhieu;

public class UserMiniDTO extends UserInfoResponseDTO {
    private String id;
    private int totalPoints;
    
    public UserMiniDTO() {
        super();
    }
    public UserMiniDTO(String id, String name, String email, boolean vip, int totalPoints) {
        super(name, email, vip); 
        this.id = id;
        this.totalPoints = totalPoints;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }
}
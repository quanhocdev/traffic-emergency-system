package com.example.suco.dto.info.user;

public class UserMiniDTO extends UserInfoResponseDTO {
    private String uid;
    
    public UserMiniDTO() {
        super();
    }
    public UserMiniDTO(String uid, String name, String email, boolean vip, int totalPoints) {
        super(name, email, vip, totalPoints); 
        this.uid = uid;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

}
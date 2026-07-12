package com.example.suco.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "doi_tien")
public class DoiTien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    private int soDiemDoi;

    private Long giaTri;

    private LocalDateTime ngayDoi;


    public DoiTien(){}


    public DoiTien(
            User user,
            int soDiemDoi,
            Long giaTri,
            LocalDateTime ngayDoi
    ){
        this.user = user;
        this.soDiemDoi = soDiemDoi;
        this.giaTri = giaTri;
        this.ngayDoi = ngayDoi;
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
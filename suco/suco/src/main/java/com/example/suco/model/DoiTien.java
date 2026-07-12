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
    @JoinColumn(name = "user_id", nullable = false)
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


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public User getUser() {
        return user;
    }


    public void setUser(User user) {
        this.user = user;
    }


    public int getSoDiemDoi() {
        return soDiemDoi;
    }


    public void setSoDiemDoi(int soDiemDoi) {
        this.soDiemDoi = soDiemDoi;
    }


    public Long getGiaTri() {
        return giaTri;
    }


    public void setGiaTri(Long giaTri) {
        this.giaTri = giaTri;
    }


    public LocalDateTime getNgayDoi() {
        return ngayDoi;
    }


    public void setNgayDoi(LocalDateTime ngayDoi) {
        this.ngayDoi = ngayDoi;
    }
}
package com.example.suco.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "quyen_gop")
public class QuyenGop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    private int soDiemQuyenGop;

    private Long giaTri;

    private String noiDung;

    private LocalDateTime ngayQuyenGop;


    public QuyenGop(){}


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


    public int getSoDiemQuyenGop() {
        return soDiemQuyenGop;
    }


    public void setSoDiemQuyenGop(int soDiemQuyenGop) {
        this.soDiemQuyenGop = soDiemQuyenGop;
    }


    public Long getGiaTri() {
        return giaTri;
    }


    public void setGiaTri(Long giaTri) {
        this.giaTri = giaTri;
    }


    public String getNoiDung() {
        return noiDung;
    }


    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }


    public LocalDateTime getNgayQuyenGop() {
        return ngayQuyenGop;
    }


    public void setNgayQuyenGop(LocalDateTime ngayQuyenGop) {
        this.ngayQuyenGop = ngayQuyenGop;
    }
}
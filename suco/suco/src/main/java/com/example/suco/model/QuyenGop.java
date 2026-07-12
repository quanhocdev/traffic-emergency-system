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
    @JoinColumn(name = "user_id")
    private User user;


    private int soDiemQuyenGop;

    private Long giaTri;


    private String noiDung;


    private LocalDateTime ngayQuyenGop;


    public QuyenGop(){}

}
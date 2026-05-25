// package com.example.suco.model;

// import jakarta.persistence.*;
// import java.time.LocalDateTime;

// @Entity
// @Table(name = "sos_dieu_phoi")
// public class SosDieuPhoi {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     // SOS gốc
//     @Column(name = "sos_id", nullable = false)
//     private Long sosId;

//     // Trụ sở được gửi
//     @Column(name = "tru_so_id", nullable = false)
//     private Long truSoId;

//     // TRẠNG THÁI CON
//     // CHO_TIEP_NHAN / TU_CHOI / TIMEOUT / TIEP_NHAN / HUY_BO
//     @Column(name = "trang_thai", length = 30)
//     private String trangThai;

//     // thứ tự trong queue (A=0, B=1, C=2...)
//     @Column(name = "thu_tu")
//     private Integer thuTu;

//     // thời điểm gửi tới trụ sở
//     @Column(name = "thoi_gian_gui")
//     private LocalDateTime thoiGianGui;

//     // thời điểm trụ sở phản hồi
//     @Column(name = "thoi_gian_xu_ly")
//     private LocalDateTime thoiGianXuLy;

//     // lý do từ chối (optional)
//     @Column(name = "ly_do", columnDefinition = "TEXT")
//     private String lyDo;

//     @PrePersist
//     public void prePersist() {
//         this.thoiGianGui = LocalDateTime.now();
//     }

//     // ================= GETTER / SETTER =================

//     public Long getId() {
//         return id;
//     }

//     public void setId(Long id) {
//         this.id = id;
//     }

//     public Long getSosId() {
//         return sosId;
//     }

//     public void setSosId(Long sosId) {
//         this.sosId = sosId;
//     }

//     public Long getTruSoId() {
//         return truSoId;
//     }

//     public void setTruSoId(Long truSoId) {
//         this.truSoId = truSoId;
//     }

//     public String getTrangThai() {
//         return trangThai;
//     }

//     public void setTrangThai(String trangThai) {
//         this.trangThai = trangThai;
//     }

//     public Integer getThuTu() {
//         return thuTu;
//     }

//     public void setThuTu(Integer thuTu) {
//         this.thuTu = thuTu;
//     }

//     public LocalDateTime getThoiGianGui() {
//         return thoiGianGui;
//     }

//     public void setThoiGianGui(LocalDateTime thoiGianGui) {
//         this.thoiGianGui = thoiGianGui;
//     }

//     public LocalDateTime getThoiGianXuLy() {
//         return thoiGianXuLy;
//     }

//     public void setThoiGianXuLy(LocalDateTime thoiGianXuLy) {
//         this.thoiGianXuLy = thoiGianXuLy;
//     }

//     public String getLyDo() {
//         return lyDo;
//     }

//     public void setLyDo(String lyDo) {
//         this.lyDo = lyDo;
//     }
// }
    package com.example.suco.model;

    import jakarta.persistence.*;
    import java.time.LocalDateTime;
    
    import com.example.suco.model.enums.TrangThaiXuLy;

    @Entity
    @Table(name = "tin_hieu_sos")
    public class TinHieuSOS {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "user_id")
        private String userId;

        @ManyToOne(fetch = FetchType.EAGER) // Thêm fetch type này
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore

    private User user;

        @Column(name = "vi_do")
        private Double viDo;

        @Column(name = "kinh_do")
        private Double kinhDo;

        @Column(name = "ghi_am")
        private String ghiAm;

        @Column(name = "hinh_anh")
        private String hinhAnh;

        @Column(name = "ghi_chu", columnDefinition = "TEXT")
        private String ghiChu;

        @Column(name = "id_tru_so_tiep_nhan")
        private Long idTruSoTiepNhan;

        // Thay đổi từ String sang Enum TrangThaiXuLy
    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai")
    private TrangThaiXuLy trangThai = TrangThaiXuLy.DA_TIEP_NHAN;
        // ----------------------

        @Column(name = "created_at")
        private LocalDateTime createdAt;
    @Column(name = "dia_chi", columnDefinition = "TEXT")
    private String diaChi;
    @Transient
    private boolean isVip; // Trường này không lưu DB, chỉ dùng để gửi ra Web
    
@OneToOne(fetch = FetchType.EAGER) // Một SOS có 1 Hóa đơn, load kèm luôn khi lấy SOS
@JoinColumn(name = "hoa_don_id") // Tạo cột hoa_don_id trong bảng tin_hieu_sos
private HoaDon hoaDon;

// Đừng quên thêm Getter/Setter cho hoaDon

public HoaDon getHoaDon() { return hoaDon; }
public void setHoaDon(HoaDon hoaDon) { this.hoaDon = hoaDon; }

    public boolean getIsVip() { return isVip; }
    public void setIsVip(boolean isVip) { this.isVip = isVip; }
    // Thêm Getter & Setter
    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
        public TinHieuSOS() {}

        @PrePersist
        protected void onCreate() {
            this.createdAt = LocalDateTime.now();
        }

        // Getter & Setter (Nhớ thêm getter/setter cho idTruSoTiepNhan và trangThai)
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
            public String getUserId() { return userId; }
            public void setUserId(String userId) { this.userId = userId; }
            public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
        public Double getViDo() { return viDo; }
        public void setViDo(Double viDo) { this.viDo = viDo; }
        public Double getKinhDo() { return kinhDo; }
        public void setKinhDo(Double kinhDo) { this.kinhDo = kinhDo; }
        public String getGhiAm() { return ghiAm; }
        public void setGhiAm(String ghiAm) { this.ghiAm = ghiAm; }
        public String getHinhAnh() { return hinhAnh; }
        public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }
        public String getGhiChu() { return ghiChu; }
        public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
        public Long getIdTruSoTiepNhan() { return idTruSoTiepNhan; }
        public void setIdTruSoTiepNhan(Long idTruSoTiepNhan) { this.idTruSoTiepNhan = idTruSoTiepNhan; }
        public TrangThaiXuLy getTrangThai() { 
        return trangThai; 
    }
    
    public void setTrangThai(TrangThaiXuLy trangThai) { 
        this.trangThai = trangThai; 
    }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
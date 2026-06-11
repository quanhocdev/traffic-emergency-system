package com.example.suco.model;

import java.time.LocalDateTime;

import com.example.suco.model.enums.MucDoSuCo;
import com.example.suco.model.enums.TrangThaiXuLy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.EnumType;

@Entity
@Table(name = "bao_cao_su_co")
public class BaoCaoSuCo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User reporter;

    @ManyToOne
    @NotNull(message = "Loại sự cố không được để trống (validation)")
    @JoinColumn(name = "loai_id")
    private LoaiSuCo loaiSuCo;

    @NotNull(message = "Kinh độ không được để trống (validation)")
    private Double kinhDo;

    @NotNull(message = "Vĩ độ không được để trống (validation)")
    private Double viDo;

    @Lob
    @NotBlank(message = "Hình ảnh không được để trống (validation)")
    @Column(columnDefinition = "LONGTEXT")
    private String hinhAnhUrl;

    private String moTa;

    // USER / ADMIN
    private String nguonBaoCao = "USER";

    private boolean aiXacNhan = false;

    // NONE, LOW, MEDIUM, HIGH
    @Enumerated(EnumType.STRING)
    @Column(name = "muc_do_su_co")
    private MucDoSuCo mucDoSuCo = MucDoSuCo.NONE;

    private LocalDateTime thoiGianTao = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
@Column(name = "trang_thai_xu_ly")
private TrangThaiXuLy trangThaiXuLy = TrangThaiXuLy.CHO_XU_LY;



@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "id_tru_so_tiep_nhan")
private TruSo truSoTiepNhan;

private Integer doTinCay = 1;

@Column(name = "dia_chi", columnDefinition = "TEXT")
private String diaChi;

    public BaoCaoSuCo() {}
    
    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getReporter() { return reporter; }
    public void setReporter(User reporter) { this.reporter = reporter; }

    public LoaiSuCo getLoaiSuCo() { return loaiSuCo; }
    public void setLoaiSuCo(LoaiSuCo loaiSuCo) { this.loaiSuCo = loaiSuCo; }

    public Double getKinhDo() { return kinhDo; }
    public void setKinhDo(Double kinhDo) { this.kinhDo = kinhDo; }

    public Double getViDo() { return viDo; }
    public void setViDo(Double viDo) { this.viDo = viDo; }

    public String getHinhAnhUrl() { return hinhAnhUrl; }
    public void setHinhAnhUrl(String hinhAnhUrl) { this.hinhAnhUrl = hinhAnhUrl; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public boolean isAiXacNhan() { return aiXacNhan; }
    public void setAiXacNhan(boolean aiXacNhan) { this.aiXacNhan = aiXacNhan; }

   public MucDoSuCo getMucDoSuCo() {
        return mucDoSuCo;
    }

    public void setMucDoSuCo(MucDoSuCo mucDoSuCo) {
        this.mucDoSuCo = mucDoSuCo;
    }

    public String getNguonBaoCao() { return nguonBaoCao; }
    public void setNguonBaoCao(String nguonBaoCao) { this.nguonBaoCao = nguonBaoCao; }

    public LocalDateTime getThoiGianTao() { return thoiGianTao; }
    public void setThoiGianTao(LocalDateTime thoiGianTao) { this.thoiGianTao = thoiGianTao; }


public TruSo getTruSoTiepNhan() {
    return truSoTiepNhan;
}

public void setTruSoTiepNhan(TruSo truSoTiepNhan) {
    this.truSoTiepNhan = truSoTiepNhan;
}
    
  public TrangThaiXuLy getTrangThaiXuLy() {
    return trangThaiXuLy;
}

public void setTrangThaiXuLy(TrangThaiXuLy trangThaiXuLy) {
    this.trangThaiXuLy = trangThaiXuLy;
}  
    public Integer getDoTinCay() { return doTinCay; }
    public void setDoTinCay(Integer doTinCay) { this.doTinCay = doTinCay; }
}

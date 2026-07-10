package com.example.suco.dto.vanhanh.camera;

public class CameraResponseDTO {

    private Long id;

    private String tenCamera;

    private Double kinhDo;

    private Double viDo;

    private String anhCamera;

    private String videoUrl;

    private String diaChi;

    // getter setter

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getTenCamera() {
        return tenCamera;
    }
    public void setTenCamera(String tenCamera) {
        this.tenCamera = tenCamera;
    }
    public Double getKinhDo() {
        return kinhDo;
    }
    public void setKinhDo(Double kinhDo) {
        this.kinhDo = kinhDo;
    }
    public Double getViDo() {
        return viDo;
    }
    public void setViDo(Double viDo) {
        this.viDo = viDo;
    }
    public String getAnhCamera() {
        return anhCamera;
    }
    public void setAnhCamera(String anhCamera) {
        this.anhCamera = anhCamera;
    }
    public String getVideoUrl() {
        return videoUrl;
    }
    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
    public String getDiaChi() {
        return diaChi;
    }
    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }
    

}

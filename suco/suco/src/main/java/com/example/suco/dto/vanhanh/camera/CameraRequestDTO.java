package com.example.suco.dto.vanhanh.camera;

import org.springframework.web.multipart.MultipartFile;

public class CameraRequestDTO {

    private String tenCamera;

    private Double kinhDo;

    private Double viDo;

    private MultipartFile anhCamera;

    private MultipartFile videoFile;

    // getter setter
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
    public MultipartFile getAnhCamera() {
        return anhCamera;
    }
    public void setAnhCamera(MultipartFile anhCamera) {
        this.anhCamera = anhCamera;
    }
    public MultipartFile getVideoFile() {
        return videoFile;
    }
    public void setVideoFile(MultipartFile videoFile) {
        this.videoFile = videoFile;
    }
    
}
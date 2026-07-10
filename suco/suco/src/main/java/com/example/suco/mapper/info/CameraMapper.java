package com.example.suco.mapper.info;
import com.example.suco.dto.vanhanh.camera.CameraMapDto;
import com.example.suco.model.Camera;
import org.springframework.stereotype.Component;
import com.example.suco.dto.vanhanh.camera.CameraRequestDTO;
import com.example.suco.dto.vanhanh.camera.CameraResponseDTO;

@Component
public class CameraMapper {

    public Camera toEntity(CameraRequestDTO dto) {

        Camera camera = new Camera();

        camera.setTenCamera(dto.getTenCamera());
        camera.setKinhDo(dto.getKinhDo());
        camera.setViDo(dto.getViDo());

        return camera;
    }

    public CameraResponseDTO toResponseDto(Camera camera) {

        CameraResponseDTO dto = new CameraResponseDTO();

        dto.setId(camera.getId());
        dto.setTenCamera(camera.getTenCamera());
        dto.setKinhDo(camera.getKinhDo());
        dto.setViDo(camera.getViDo());
        dto.setAnhCamera(camera.getAnhCamera());
        dto.setVideoUrl(camera.getVideoUrl());
        dto.setDiaChi(camera.getDiaChi());

        return dto;
    }

    public CameraMapDto toMapDto(Camera camera) {

        return new CameraMapDto(
                camera.getId(),
                camera.getTenCamera(),
                camera.getKinhDo(),
                camera.getViDo(),
                camera.getAnhCamera(),
                camera.getVideoUrl(),
                camera.getDiaChi(),
                0.0
        );
    }

    public CameraMapDto toMapDto(Camera camera, double distance) {

        return new CameraMapDto(
                camera.getId(),
                camera.getTenCamera(),
                camera.getKinhDo(),
                camera.getViDo(),
                camera.getAnhCamera(),
                camera.getVideoUrl(),
                camera.getDiaChi(),
                distance
        );
    }
}
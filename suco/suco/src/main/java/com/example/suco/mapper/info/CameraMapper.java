package com.example.suco.mapper.info;
import com.example.suco.dto.vanhanh.camera.CameraMapDto;
import com.example.suco.model.Camera;
import org.springframework.stereotype.Component;

@Component
public class CameraMapper {

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
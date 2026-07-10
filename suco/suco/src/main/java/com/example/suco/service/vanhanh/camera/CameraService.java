package com.example.suco.service.vanhanh.camera;

import com.example.suco.dto.vanhanh.camera.CameraMapDto;
import com.example.suco.mapper.info.CameraMapper;
import com.example.suco.model.Camera;
import com.example.suco.repository.vanhanh.CameraRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.example.suco.service.file.FileStorageService;
import com.example.suco.service.location.GeocodingService;
import java.util.List;
import java.util.stream.Collectors;
import ch.hsr.geohash.GeoHash;


@Service
public class CameraService {
    @Autowired
    private CameraRepository cameraRepository;

    @Autowired
private CameraMapper cameraMapper;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
private GeocodingService geocodingService;

@Autowired
private FileStorageService fileStorageService;

@Autowired
private CameraNearService cameraNearService;


    // 1. Lấy tất cả camera (Dạng Model cho Admin quản lý bảng)
    public List<Camera> getAllCameras() {
        return cameraRepository.findAll();
    }

    // 2. Lấy danh sách camera chưa gán tọa độ (Dùng cho Select-box trên FE)
    public List<Camera> getCamerasChuaGan() {
        return cameraRepository.findAll().stream()
                .filter(c -> c.getKinhDo() == null || c.getKinhDo() == 0.0)
                .collect(Collectors.toList());
    }

    // 3. Lấy tất cả camera dưới dạng DTO (Dùng cho Mapbox API)
    public List<CameraMapDto> getAllCameraForMap() {
    return cameraRepository.findAll().stream()
            .filter(c -> c.getKinhDo() != null && c.getKinhDo() != 0.0)
            .map(cameraMapper::toMapDto)
                
            .collect(Collectors.toList());
}

    // 4. Lưu hoặc Cập nhật vị trí Camera
    @Transactional
    public Camera saveCamera(Camera camera) {
        // TÍNH TOÁN GEOHASH 8 (Đảm bảo luôn tính trước khi lưu)
        if (camera.getViDo() != null
        && camera.getKinhDo() != null
        && camera.getViDo() != 0) {

    String gh = GeoHash.withCharacterPrecision(
            camera.getViDo(),
            camera.getKinhDo(),
            8
    ).toBase32();

    camera.setGeohash(gh);

    camera.setDiaChi(
            geocodingService.getAddress(
                    camera.getViDo(),
                    camera.getKinhDo()
            )
    );
}

        Camera saved;
        if (camera.getId() != null) {
            saved = cameraRepository.findById(camera.getId())
                    .map(existing -> {
                        existing.setKinhDo(camera.getKinhDo());
                        existing.setViDo(camera.getViDo());
                        existing.setGeohash(camera.getGeohash()); // CẬP NHẬT CẢ GEOHASH VÀO DB
                        existing.setDiaChi(camera.getDiaChi());
                        if (camera.getTenCamera() != null && !camera.getTenCamera().isEmpty()) {
                            existing.setTenCamera(camera.getTenCamera());
                        }
                        return cameraRepository.save(existing);
                    }).orElseGet(() -> cameraRepository.save(camera));
        } else {
            if (camera.getKinhDo() == null) camera.setKinhDo(0.0);
            if (camera.getViDo() == null) camera.setViDo(0.0);
            saved = cameraRepository.save(camera);
        }

        CameraMapDto dto = cameraMapper.toMapDto(saved);
        messagingTemplate.convertAndSend("/topic/camera", dto);
        return saved;
    }

    
    

    // 5. Xóa Camera
    @Transactional
    public void deleteCamera(Long id) {
        cameraRepository.deleteById(id);
        messagingTemplate.convertAndSend("/topic/camera-delete", id);
    }

    // 6. Lưu ảnh camera
    public String saveImage(MultipartFile imageFile) {
    return fileStorageService.saveMultipart(
            imageFile,
            "cameras"
    );
    }

    // 7. Lưu video demo camera
    public String saveVideo(MultipartFile file) {
    return fileStorageService.saveMultipart(
            file,
            "cameras/videos"
    );
    }
    private double tinhKhoangCach(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Bán kính Trái Đất (km)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
    
}
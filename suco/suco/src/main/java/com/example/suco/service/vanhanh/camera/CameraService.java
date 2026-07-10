package com.example.suco.service.vanhanh.camera;

import com.example.suco.dto.vanhanh.camera.CameraMapDto;
import com.example.suco.dto.vanhanh.camera.CameraRequestDTO;
import com.example.suco.dto.vanhanh.camera.CameraResponseDTO;
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


    public CameraResponseDTO createCamera(CameraRequestDTO dto){

    Camera camera = cameraMapper.toEntity(dto);

    if(dto.getAnhCamera() != null && !dto.getAnhCamera().isEmpty()){
        camera.setAnhCamera(
                fileStorageService.saveMultipart(
                        dto.getAnhCamera(),
                        "cameras"
                )
        );
    }


    if(dto.getVideoFile() != null && !dto.getVideoFile().isEmpty()){
        camera.setVideoUrl(
                fileStorageService.saveMultipart(
                        dto.getVideoFile(),
                        "cameras/videos"
                )
        );
    }

    Camera saved = saveCamera(camera);

    return cameraMapper.toResponseDto(saved);
}

public CameraResponseDTO updateCamera(Long id, CameraRequestDTO dto) {

    Camera camera = cameraRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy camera"));

    // cập nhật tên
    camera.setTenCamera(dto.getTenCamera());

    // cập nhật tọa độ
    camera.setKinhDo(dto.getKinhDo());
    camera.setViDo(dto.getViDo());

    // cập nhật ảnh nếu có
    if (dto.getAnhCamera() != null && !dto.getAnhCamera().isEmpty()) {
        camera.setAnhCamera(
                fileStorageService.saveMultipart(dto.getAnhCamera(), "cameras")
        );
    }

    // cập nhật video nếu có
    if (dto.getVideoFile() != null && !dto.getVideoFile().isEmpty()) {
        camera.setVideoUrl(
                fileStorageService.saveMultipart(dto.getVideoFile(), "cameras/videos")
        );
    }

    Camera saved = saveCamera(camera);

    return cameraMapper.toResponseDto(saved);
}

public CameraResponseDTO getCameraDetail(Long id){

    Camera camera = cameraRepository.findById(id)
            .orElseThrow(
                () -> new RuntimeException("Không tìm thấy camera")
            );

    return cameraMapper.toResponseDto(camera);
}

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

    if (!cameraRepository.existsById(id)) {
        throw new RuntimeException("Camera không tồn tại");
    }

    cameraRepository.deleteById(id);

    messagingTemplate.convertAndSend(
            "/topic/camera-delete",
            id
    );
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
    
}
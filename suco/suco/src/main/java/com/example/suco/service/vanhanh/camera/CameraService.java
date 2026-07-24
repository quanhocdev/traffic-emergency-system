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
import com.example.suco.service.file.FileStorageService;
import com.example.suco.service.geohash.GeoHashService;
import com.example.suco.service.location.GeocodingService;
import java.util.List;
import java.util.stream.Collectors;


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
private GeoHashService geoHashHelperService;


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
public CameraResponseDTO updateLocation(
        Long id,
        double kinhDo,
        double viDo
){

    Camera camera = cameraRepository.findById(id)
            .orElseThrow(
                () -> new RuntimeException("Không tìm thấy camera")
            );

    camera.setKinhDo(kinhDo);
    camera.setViDo(viDo);

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
   public List<CameraResponseDTO> getAllCameras() {
    return cameraRepository.findAll()
            .stream()
            .map(cameraMapper::toResponseDto)
            .toList();
}
    // 2. Lấy danh sách camera chưa gán tọa độ 
    public List<CameraResponseDTO> getCamerasChuaGan() {
        return cameraRepository.findAll().stream()
                .filter(c -> c.getKinhDo() == null || c.getKinhDo() == 0.0)
                .map(cameraMapper::toResponseDto)
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
private Camera saveCamera(Camera camera) {

    if (camera.getViDo() != null
            && camera.getKinhDo() != null
            && camera.getViDo() != 0) {

        camera.setGeohash(
                geoHashHelperService.getGeoHash(
                    camera.getViDo(),
                    camera.getKinhDo(),
                    8
                )
        );
        camera.setDiaChi(
                geocodingService.getAddress(
                        camera.getViDo(),
                        camera.getKinhDo()
                )
        );
    }


    Camera saved = cameraRepository.save(camera);


    messagingTemplate.convertAndSend(
            "/topic/camera",
            cameraMapper.toMapDto(saved)
    );


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
    
}
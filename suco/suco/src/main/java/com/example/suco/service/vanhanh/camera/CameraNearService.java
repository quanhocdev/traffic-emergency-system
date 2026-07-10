package com.example.suco.service.vanhanh.camera;

import ch.hsr.geohash.GeoHash;
import com.example.suco.dto.vanhanh.camera.CameraMapDto;
import com.example.suco.mapper.info.CameraMapper;
import com.example.suco.model.Camera;
import com.example.suco.repository.vanhanh.CameraRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CameraNearService {

    private static final Logger log =
            LoggerFactory.getLogger(CameraNearService.class);

    @Autowired
    private CameraRepository cameraRepository;

    @Autowired
    private CameraMapper cameraMapper;

    /**
     * Tìm camera trong bán kính 20m quanh sự cố
     */
    public List<CameraMapDto> getCamerasNearIncident(
            double lat,
            double lng
    ) {

        log.info("========== KIỂM TRA CAMERA GẦN SỰ CỐ ==========");
        log.info("[Vị trí sự cố]: {}, {}", lat, lng);

        // 1. Geohash của vị trí sự cố
        GeoHash center = GeoHash.withCharacterPrecision(lat, lng, 8);

        List<String> area = new ArrayList<>();
        area.add(center.toBase32());

        for (GeoHash adjacent : center.getAdjacent()) {
            area.add(adjacent.toBase32());
        }

        log.info(
                "[Geohash]: Quét vùng 9 ô xung quanh mã: {}",
                center.toBase32()
        );

        // 2. Lấy các camera trong 9 ô Geohash
        List<Camera> candidates =
                cameraRepository.findByGeohashIn(area);

        log.info(
                "[DB]: Tìm thấy {} camera tiềm năng",
                candidates.size()
        );

        // 3. Lọc theo Haversine (20m)
        List<CameraMapDto> result = candidates.stream()
                .map(camera -> {

                    double distance = tinhKhoangCach(
                            lat,
                            lng,
                            camera.getViDo(),
                            camera.getKinhDo()
                    );

                    log.info(
                            "Camera {} distance = {} km",
                            camera.getId(),
                            distance
                    );

                    if (distance > 0.02) {
                        return null;
                    }

                    return cameraMapper.toMapDto(
                            camera,
                            Math.round(distance * 1000 * 100.0) / 100.0
                    );

                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());

        log.info(
                "[Kết quả]: Tổng cộng {} camera khả dụng.",
                result.size()
        );

        log.info("===============================================");

        return result;
    }

    /**
     * Tính khoảng cách Haversine (km)
     */
    private double tinhKhoangCach(
            double lat1,
            double lon1,
            double lat2,
            double lon2
    ) {

        double R = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2)
                        + Math.cos(Math.toRadians(lat1))
                        * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2)
                        * Math.sin(dLon / 2);

        return R * 2 * Math.atan2(
                Math.sqrt(a),
                Math.sqrt(1 - a)
        );
    }
}
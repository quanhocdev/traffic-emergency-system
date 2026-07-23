package com.example.suco.service.vanhanh.camera;

import com.example.suco.dto.vanhanh.camera.CameraMapDto;
import com.example.suco.mapper.info.CameraMapper;
import com.example.suco.model.Camera;
import com.example.suco.repository.vanhanh.CameraRepository;
import com.example.suco.service.distance.DistanceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.suco.service.geohash.GeoHashService;
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
        
        @Autowired
        private DistanceService distanceService;


        @Autowired
        private GeoHashService geoHashHelperService;

        /**
         * Tìm camera trong bán kính 20m quanh sự cố
         */
        public List<CameraMapDto> getCamerasNearIncident(
                double lat,
                double lng
        ) {

                log.info("========== KIỂM TRA CAMERA GẦN SỰ CỐ ==========");
                log.info("[Vị trí sự cố]: {}, {}", lat, lng);

                // 1. Geohash lấy trung tâm và 8 ô lân cận
                List<String> area =
                geoHashHelperService.getNeighborPrefixes(lat, lng, 8);

                log.info("[Geohash]: {}", area);

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

                        double distance = distanceService.calculateDistanceInKm(
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

        }
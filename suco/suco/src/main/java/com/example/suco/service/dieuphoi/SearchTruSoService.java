package com.example.suco.service.dieuphoi;

import ch.hsr.geohash.GeoHash;
import com.example.suco.model.TruSo;
import com.example.suco.repository.vanhanh.TruSoRepository;
import com.example.suco.service.distance.DistanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.suco.service.geohash.GeoHashService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchTruSoService {

    private static final Logger log =
            LoggerFactory.getLogger(SearchTruSoService.class);

    @Autowired
    private TruSoRepository truSoRepository;

    @Autowired
    private DistanceService distanceService;

    @Autowired
    private GeoHashService geoHashHelperService;

    public List<TruSo> findTruSoInArea(double lat, double lng) {

        List<TruSo> result = new ArrayList<>();
        int precision = 6;

        while (precision >= 4 && result.isEmpty()) {

            GeoHash center = GeoHash.withCharacterPrecision(lat, lng, precision);
            String base = center.toBase32();

            List<String> prefixes =
                geoHashHelperService.getNeighborPrefixes(lat, lng, precision);

            List<TruSo> temp = new ArrayList<>();

            
            for (String prefix : prefixes) {
                temp.addAll(truSoRepository.findByGeohashStartingWith(prefix));
            }

            result = temp;

            log.info("========== GEO LEVEL {} ==========", precision);
            log.info("Center base32: {}", base);
            log.info("Prefixes: {}", prefixes);

            if (result.isEmpty()) {
                log.info("[Geo {}] NO TRU SO FOUND", precision);
            } else {

                result.sort(Comparator.comparingDouble(ts ->
                        distanceService.calculateDistanceInMeters(
                                lat,
                                lng,
                                ts.getViDo(),
                                ts.getKinhDo()
                        )
                ));

                log.info("[Geo {}] FOUND {} TRU SO", precision, result.size());
            }

            precision--;
        }

        // Không tìm thấy trong bất kỳ GeoHash nào
        if (result.isEmpty()) {
            log.warn("NO GEO MATCH -> fallback to all truso");
            return truSoRepository.findAll();
        }

        // Loại bỏ trùng
        Map<Long, TruSo> uniqueMap = new LinkedHashMap<>();

        for (TruSo truSo : result) {
            uniqueMap.put(truSo.getId(), truSo);
        }

        result = new ArrayList<>(uniqueMap.values());

        // Sắp xếp lại theo khoảng cách
        result.sort(Comparator.comparingDouble(ts ->
                distanceService.calculateDistanceInMeters(
                        lat,
                        lng,
                        ts.getViDo(),
                        ts.getKinhDo()
                )
        ));

        return result;
    }
}
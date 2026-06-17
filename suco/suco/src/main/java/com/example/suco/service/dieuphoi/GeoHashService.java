package com.example.suco.service.dieuphoi;

import com.example.suco.model.TruSo;
import com.example.suco.repository.vanhanh.TruSoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ch.hsr.geohash.GeoHash;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class GeoHashService {

    @Autowired
    private TruSoRepository truSoRepository;

    @Autowired
    private DieuPhoiDistanceService distanceService;

    private static final Logger log = LoggerFactory.getLogger(GeoHashService.class);

    public List<TruSo> findTruSoInArea(double lat, double lng) {

    List<TruSo> result = new ArrayList<>();
    int precision = 6;

    while (precision >= 4 && result.isEmpty()) {

        GeoHash center = GeoHash.withCharacterPrecision(lat, lng, precision);
        String base = center.toBase32();

        List<TruSo> temp = new ArrayList<>();

        temp.addAll(truSoRepository.findByGeohashStartingWith(base));

        List<String> prefixes = new ArrayList<>();
        prefixes.add(base);

        for (GeoHash h : center.getAdjacent()) {
            String p = h.toBase32();
            prefixes.add(p);
            temp.addAll(truSoRepository.findByGeohashStartingWith(p));
        }

        result = temp;  

        log.info("========== GEO LEVEL {} ==========", precision);
        log.info("Center base32: {}", base);
        log.info("Prefixes: {}", prefixes);

        if (result.isEmpty()) {
            log.info("[Geo {}] NO TRU SO FOUND", precision);
        } else {

            result.sort(Comparator.comparingDouble(ts ->
                    distanceService.distance(lat, lng, ts.getViDo(), ts.getKinhDo())
            ));

            log.info("[Geo {}] FOUND {} TRU SO", precision, result.size());
        }

        precision--;
    }

    if (result.isEmpty()) {
    log.warn("NO GEO MATCH -> fallback to all truso");
    return truSoRepository.findAll();
}

// dedup
Map<Long, TruSo> map = new LinkedHashMap<>();
for (TruSo t : result) {
    map.put(t.getId(), t);
}
result = new ArrayList<>(map.values());

// sort theo khoảng cách
result.sort(Comparator.comparingDouble(ts ->
        distanceService.distance(lat, lng, ts.getViDo(), ts.getKinhDo())
));

return result;
}

}
package com.example.suco.service.dieuphoi.geohash;

import com.example.suco.model.TruSo;
import com.example.suco.repository.TruSoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ch.hsr.geohash.GeoHash;
import com.example.suco.service.dieuphoi.distance.DistanceService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class GeoHashService {

    @Autowired
    private TruSoRepository truSoRepository;

    @Autowired
    private DistanceService distanceService;

    private static final Logger log = LoggerFactory.getLogger(GeoHashService.class);



    public List<TruSo> findTruSoInArea(double lat, double lng) {

        List<TruSo> result = new ArrayList<>();
        int precision = 6;

        while (precision >= 4 && result.isEmpty()) {

            GeoHash center = GeoHash.withCharacterPrecision(lat, lng, precision);
            String base = center.toBase32();

            List<String> prefixes = new ArrayList<>();
            prefixes.add(base);

            for (GeoHash h : center.getAdjacent()) {
                prefixes.add(h.toBase32());
            }

            result = truSoRepository.findByGeohashIn(prefixes);
            // ================= LOG GEO LEVEL =================
log.info("========== GEO LEVEL {} ==========", precision);
log.info("Center base32: {}", base);
log.info("Prefixes: {}", prefixes);

if (result.isEmpty()) {
    log.info("[Geo {}] NO TRU SO FOUND", precision);
} else {

    // ===== build list kèm distance =====
    List<String> debugList = new ArrayList<>();

    List<TruSo> sorted = new ArrayList<>(result);

    sorted.sort((a, b) -> {
        double da = distanceService.distance(lat, lng, a.getViDo(), a.getKinhDo());
        double db = distanceService.distance(lat, lng, b.getViDo(), b.getKinhDo());
        return Double.compare(da, db);
    });

    double bestDistance = Double.MAX_VALUE;
    Long bestId = null;

    for (TruSo ts : sorted) {

        double d = distanceService.distance(
                lat, lng,
                ts.getViDo(),
                ts.getKinhDo()
        );

        double km = d / 1000.0;

        if (d < bestDistance) {
            bestDistance = d;
            bestId = ts.getId();
        }

        debugList.add("ID=" + ts.getId() + " | " + Math.round(km * 100.0) / 100.0 + " km");

        log.info("[Geo {}] TRU_SO_ID={} | distance={} km",
                precision,
                ts.getId(),
                Math.round(km * 100.0) / 100.0
        );
    }

    // ===== BEST candidate log =====
    log.info("[Geo {}] BEST candidate -> TRU_SO_ID={} | distance={} km",
            precision,
            bestId,
            Math.round(bestDistance / 1000.0 * 100.0) / 100.0
    );
}

            if (result.isEmpty()) {
                precision--;
            }
        }

        if (result.isEmpty()) {
            return truSoRepository.findAll();
        }

        return new ArrayList<>(new HashSet<>(result));
    }


}
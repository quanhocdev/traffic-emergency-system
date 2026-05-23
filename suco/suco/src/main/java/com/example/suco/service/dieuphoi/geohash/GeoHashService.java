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

@Service
public class GeoHashService {

    @Autowired
    private TruSoRepository truSoRepository;

    @Autowired
    private DistanceService distanceService;

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

            if (result.isEmpty()) {
                precision--;
            }
        }

        if (result.isEmpty()) {
            return truSoRepository.findAll();
        }

        return new ArrayList<>(new HashSet<>(result));
    }
     // =====================================================
    // 🔥 NEW: FAST PATH ≤ 5km ONLY
    // =====================================================
    public List<TruSo> findTruSoFastPath(double lat, double lng) {

        List<TruSo> candidates = findTruSoInArea(lat, lng);

        List<TruSo> fast = new ArrayList<>();

        for (TruSo ts : candidates) {

            double d = distanceService.distance(
                    lat, lng,
                    ts.getViDo(), ts.getKinhDo()
            );

            if (d <= 5000) {
                fast.add(ts);
            }
        }

        return fast;
    }

    // =====================================================
    // OPTIONAL: 5–15km pool (for your queue system)
    // =====================================================
    public List<TruSo> findTruSoMidRange(double lat, double lng) {

        List<TruSo> candidates = findTruSoInArea(lat, lng);

        List<TruSo> mid = new ArrayList<>();

        for (TruSo ts : candidates) {

            double d = distanceService.distance(
                    lat, lng,
                    ts.getViDo(), ts.getKinhDo()
            );

            if (d > 5000 && d <= 15000) {
                mid.add(ts);
            }
        }

        return mid;
    }

}
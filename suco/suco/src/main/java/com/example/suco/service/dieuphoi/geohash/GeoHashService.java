package com.example.suco.service.dieuphoi.geohash;

import com.example.suco.model.TruSo;
import com.example.suco.repository.TruSoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ch.hsr.geohash.GeoHash;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class GeoHashService {

    @Autowired
    private TruSoRepository truSoRepository;

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
}
package com.example.suco.service.geohash;

import ch.hsr.geohash.GeoHash;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GeoHashHelperService {

    /* Trả về danh sách GeoHash gồm: Ô trung tâm và 8 ô lân cận */
    public List<String> getNeighborPrefixes(
            double lat,
            double lng,
            int precision
    ) {

        GeoHash center = GeoHash.withCharacterPrecision(lat, lng, precision);

        List<String> prefixes = new ArrayList<>();
        prefixes.add(center.toBase32());

        for (GeoHash adjacent : center.getAdjacent()) {
            prefixes.add(adjacent.toBase32());
        }

        return prefixes;
    }

}
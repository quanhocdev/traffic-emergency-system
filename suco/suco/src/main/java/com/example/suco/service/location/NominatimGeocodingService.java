package com.example.suco.service.location;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NominatimGeocodingService implements GeocodingService {

    @Autowired
    private NominatimGeocodingClient geocodingUtil;

    @Override
    public String getAddress(Double viDo, Double kinhDo) {

    if (viDo == null || kinhDo == null) {
        return "Không xác định vị trí";
    }

    try {
        var addrMap = geocodingUtil.getAddressFromCoordinates(viDo, kinhDo);

        String formatted = geocodingUtil.formatAddress(addrMap);

        return (formatted == null || formatted.isBlank())
                ? "Tọa độ: " + viDo + ", " + kinhDo
                : formatted;

    } catch (Exception e) {
        return "Tọa độ: " + viDo + ", " + kinhDo;
    }
}
}
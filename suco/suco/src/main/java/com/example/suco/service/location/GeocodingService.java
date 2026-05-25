package com.example.suco.service.location;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.suco.util.GeocodingUtil;

@Service
public class GeocodingService {

    @Autowired
    private GeocodingUtil geocodingUtil;

    public String getAddress(Double viDo, Double kinhDo) {

        try {
            var addrMap = geocodingUtil.getAddressFromCoordinates(
                    viDo,
                    kinhDo
            );
            return geocodingUtil.formatAddress(addrMap);
        } catch (Exception e) {
            return "Tọa độ: " + viDo + ", " + kinhDo;
        }
    }
}
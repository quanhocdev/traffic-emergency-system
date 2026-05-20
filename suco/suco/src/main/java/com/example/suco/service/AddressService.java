package com.example.suco.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.suco.dto.TinHieuSOSRequestDTO;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.util.GeocodingUtil;

@Service
public class AddressService {

    @Autowired
    private GeocodingUtil geocodingUtil;

    /**
     * Lấy địa chỉ chi tiết từ kinh độ, vĩ độ
     * @param latitude vĩ độ
     * @param longitude kinh độ
     * @return Map chứa: tenDuong, quan, huyenHoac, thanhPho, diaChi
     */
    public Map<String, String> getAddressDetails(Double latitude, Double longitude) {
        return geocodingUtil.getAddressFromCoordinates(latitude, longitude);
    }

    /**
     * Lấy địa chỉ dạng chuỗi formatted
     * @param latitude vĩ độ
     * @param longitude kinh độ
     * @return Chuỗi địa chỉ
     */
    public String getFormattedAddress(Double latitude, Double longitude) {
        Map<String, String> addressMap = geocodingUtil.getAddressFromCoordinates(latitude, longitude);
        return geocodingUtil.formatAddress(addressMap);
    }
    public String resolveAddress(TinHieuSOS sos, TinHieuSOSRequestDTO dto) {
    if (dto.getDiaChi() != null && !dto.getDiaChi().isEmpty()) {
        return dto.getDiaChi();
    }

    try {
        var addr = geocodingUtil.getAddressFromCoordinates(
                sos.getViDo(), sos.getKinhDo()
        );
        return geocodingUtil.formatAddress(addr);
    } catch (Exception e) {
        return "SOS: " + sos.getViDo() + ", " + sos.getKinhDo();
    }
}
}

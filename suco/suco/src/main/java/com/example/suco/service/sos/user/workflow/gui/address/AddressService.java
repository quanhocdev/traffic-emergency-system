package com.example.suco.service.sos.user.workflow.gui.address;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.suco.dto.TinHieuSOSRequestDTO;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.util.GeocodingUtil;


@Service
public class AddressService {

    @Autowired
    private GeocodingUtil geocodingUtil;

    public void handleAddress(TinHieuSOS sos, TinHieuSOSRequestDTO dto) {
    if (dto.getDiaChi() != null && !dto.getDiaChi().isEmpty()) {
        sos.setDiaChi(dto.getDiaChi());
        return;
    }

    try {
        var addr = geocodingUtil.getAddressFromCoordinates(
                sos.getViDo(), sos.getKinhDo()
        );
        sos.setDiaChi(geocodingUtil.formatAddress(addr));
    } catch (Exception e) {
        sos.setDiaChi("SOS: " + sos.getViDo() + ", " + sos.getKinhDo());
    }
}
    
}

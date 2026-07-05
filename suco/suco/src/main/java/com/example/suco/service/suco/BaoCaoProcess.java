package com.example.suco.service.suco;

import com.example.suco.dto.suco.baocao.user.SuCoRequestDTO;
import com.example.suco.model.BaoCaoSuCo;

public interface BaoCaoProcess {
        BaoCaoSuCo process(SuCoRequestDTO dto, String uid, Object extraData);
}


// Image Storage, Notification, Geocoding
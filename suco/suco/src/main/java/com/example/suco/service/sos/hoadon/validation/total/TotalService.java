package com.example.suco.service.sos.hoadon.validation.total;

import com.example.suco.model.Goi;
import com.example.suco.model.TruSo;
import com.example.suco.service.sos.hoadon.validation.DistanceService;
import com.example.suco.model.TinHieuSOS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;


@Service
public class TotalService {

    @Autowired
    private DistanceService distanceService;

    public BigDecimal calculate(
            Goi goi,
            TruSo truso,
            TinHieuSOS sos
    ) {

        double distance = distanceService.calculateDistance(
                truso.getViDo(),
                truso.getKinhDo(),
                sos.getViDo(),
                sos.getKinhDo()
        );

        double freeKm = (goi.getKhoangCachMienPhi() != null)
                ? goi.getKhoangCachMienPhi()
                : 0;

        double extraKm = Math.max(0, distance - freeKm);

        long kmTinhTien = (long) Math.ceil(extraKm);

        return BigDecimal.valueOf(kmTinhTien * 10000);
    }
}

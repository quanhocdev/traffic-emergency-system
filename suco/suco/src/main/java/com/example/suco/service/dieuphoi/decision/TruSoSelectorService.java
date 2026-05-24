package com.example.suco.service.dieuphoi.decision;

import com.example.suco.model.TruSo;
import com.example.suco.model.enums.TrangThaiHoatDongTruSo;
import com.example.suco.repository.TruSoRepository;
import com.example.suco.service.dieuphoi.distance.DistanceService;
import com.example.suco.service.dieuphoi.geohash.GeoHashService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class TruSoSelectorService {

    @Autowired
    private GeoHashService geoHashService;

    @Autowired
    private DistanceService distanceService;

    @Autowired
    private TruSoRepository truSoRepository;

    /**
     * CHỌN TRỤ SỞ GẦN NHẤT (single decision point)
     */
    private boolean isAvailable(TruSo truSo) {

    return truSo.getDangNhanTinHieu()

            && truSo.getTrangThaiHoatDong()
            == TrangThaiHoatDongTruSo.SAN_SANG

            && truSo.getSoLuongDangXuLy()
            < truSo.getGioiHanXuLy();
}

    public TruSo selectNearest(double lat, double lng) {

        List<TruSo> candidates = geoHashService.findTruSoInArea(lat, lng);

        if (candidates == null || candidates.isEmpty()) {
            candidates = truSoRepository.findAll();
        }

        return candidates.stream()
                .filter(this::isAvailable)
                .min(Comparator.comparingDouble(ts ->
                        distanceService.distance(lat, lng, ts.getViDo(), ts.getKinhDo())
                ))
                .orElse(null);
    }

    /**
     * TRẢ DANH SÁCH ĐÃ SORT (cho SOS / điều phối)
     */
    public List<TruSo> selectSorted(double lat, double lng) {

        List<TruSo> candidates = geoHashService.findTruSoInArea(lat, lng);

        return candidates.stream()
        
                .sorted(Comparator.comparingDouble(ts ->
                        distanceService.distance(lat, lng, ts.getViDo(), ts.getKinhDo())
                ))
                .toList();
    }
}
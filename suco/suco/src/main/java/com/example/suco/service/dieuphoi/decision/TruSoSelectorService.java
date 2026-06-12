package com.example.suco.service.dieuphoi.decision;

import com.example.suco.model.TruSo;
import com.example.suco.model.enums.TrangThaiHoatDongTruSo;
import com.example.suco.repository.vanhanh.TruSoRepository;
import com.example.suco.service.dieuphoi.distance.DieuPhoiDistanceService;
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
    private DieuPhoiDistanceService distanceService;

    @Autowired
    private TruSoRepository truSoRepository;

 private boolean isAvailable(TruSo truSo) {

    return truSo.getTrangThaiHoatDong()
            == TrangThaiHoatDongTruSo.SAN_SANG;
}

    // Trả về trụ sở gần nhất trong bán kính tìm kiếm, nếu không có thì trả về trụ sở gần nhất trên toàn bộ hệ thống    
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

    // Trả danh sách trụ sở đã sắp xếp
    public List<TruSo> selectSorted(double lat, double lng) {

        List<TruSo> candidates = geoHashService.findTruSoInArea(lat, lng);

        return candidates.stream()
        
                .sorted(Comparator.comparingDouble(ts ->
                        distanceService.distance(lat, lng, ts.getViDo(), ts.getKinhDo())
                ))
                .toList();
    }
}
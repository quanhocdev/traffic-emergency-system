package com.example.suco.service.dieuphoi.queue;

import com.example.suco.model.TruSo;
import com.example.suco.service.dieuphoi.distance.DistanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class DispatchQueueService {

    @Autowired
    private DistanceService distanceService;

    public List<Long> buildQueue(List<TruSo> danhSach, double lat, double lng) {

        return danhSach.stream()
                .sorted(Comparator.comparingDouble(ts ->
                        distanceService.distance(lat, lng, ts.getViDo(), ts.getKinhDo())
                ))
                .map(TruSo::getId)
                .toList();
    }
}
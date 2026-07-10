package com.example.suco.service.distance;

public interface DistanceService {

    /**
     * Tính khoảng cách giữa 2 tọa độ theo đơn vị Kilômét (km).
     */
    double calculateDistanceInKm(
            double lat1,
            double lon1,
            double lat2,
            double lon2
    );

    /**
     * Tính khoảng cách giữa 2 tọa độ theo đơn vị Mét (m).
     */
    double calculateDistanceInMeters(
            double lat1,
            double lon1,
            double lat2,
            double lon2
    );
}
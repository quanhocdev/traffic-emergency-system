package com.example.suco.repository.sos.hoadon;

import com.example.suco.model.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface HoaDonCuuHoRepository extends JpaRepository<HoaDon, Long> {
    Optional<HoaDon> findBySosId(Long sosId);
    Optional<HoaDon> findFirstBySosIdOrderByIdDesc(Long sosId);
    List<HoaDon> findByTrusoIdOrderByIdDesc(Long trusoId);
    List<HoaDon> findByUserIdOrderByIdDesc(String userId);
    @Query("SELECT h FROM HoaDon h LEFT JOIN FETCH h.thanhToans WHERE h.id = :id")
Optional<HoaDon> findDetailById(Long id);
@Query("SELECT DISTINCT h FROM HoaDon h LEFT JOIN FETCH h.thanhToans ORDER BY h.id DESC")
List<HoaDon> findAllWithPayments();
}
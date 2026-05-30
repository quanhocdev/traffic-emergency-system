package com.example.suco.repository.sos.hoadon;

import com.example.suco.model.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface HoaDonCuuHoRepository extends JpaRepository<HoaDon, Long> {
    Optional<HoaDon> findBySosId(Long sosId);
    Optional<HoaDon> findFirstBySosIdOrderByIdDesc(Long sosId);
    List<HoaDon> findByTrusoIdOrderByIdDesc(Long trusoId);
    List<HoaDon> findByUserIdOrderByIdDesc(String userId);
}
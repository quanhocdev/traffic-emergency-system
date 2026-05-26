package com.example.suco.repository.suco.loai;

import com.example.suco.model.LoaiSuCo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface LoaiSuCoRepository extends JpaRepository<LoaiSuCo, Long> {
    Optional<LoaiSuCo> findByTen(String ten);
}

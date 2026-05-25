package com.example.suco.repository;

import com.example.suco.model.TruSo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;

public interface TruSoRepository extends JpaRepository<TruSo, Long> {
    Optional<TruSo> findByTenDangNhap(String tenDangNhap);
    
    // Giữ lại cái cũ nếu cần
    List<TruSo> findByGeohashStartingWith(String prefix);

@Query("SELECT t FROM TruSo t WHERE SUBSTRING(t.geohash, 1, 6) IN :prefixes")
List<TruSo> findByGeohashIn(@org.springframework.data.repository.query.Param("prefixes") List<String> prefixes);
    boolean existsByTenDangNhap(String tenDangNhap);

}
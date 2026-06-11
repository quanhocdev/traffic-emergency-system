package com.example.suco.repository.suco.baocao;

import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.model.enums.TrangThaiXuLy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface BaoCaoSuCoRepository extends JpaRepository<BaoCaoSuCo, Long> {

    // =========================
    // 1. ADMIN / USER MAP
    // =========================
    @Query("""
    SELECT DISTINCT b FROM BaoCaoSuCo b
    LEFT JOIN FETCH b.loaiSuCo
    LEFT JOIN FETCH b.reporter
    LEFT JOIN FETCH b.truSoTiepNhan
    WHERE b.trangThaiXuLy NOT IN ('HOAN_THANH', 'HUY_BO')
""")
List<BaoCaoSuCo> findAllForMapEntity();

List<BaoCaoSuCo> findByTrangThaiXuLyNotIn(List<TrangThaiXuLy> statuses);    
    List<BaoCaoSuCo> findByReporterUid(String uid);
}
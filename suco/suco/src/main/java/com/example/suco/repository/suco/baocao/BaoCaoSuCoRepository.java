package com.example.suco.repository.suco.baocao;

import com.example.suco.model.BaoCaoSuCo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BaoCaoSuCoRepository extends JpaRepository<BaoCaoSuCo, Long> {

    // =========================
    // 1. ADMIN / USER MAP
    // =========================
    @Query("""
        SELECT DISTINCT b FROM BaoCaoSuCo b
        LEFT JOIN FETCH b.loaiSuCo
        LEFT JOIN FETCH b.reporter
        LEFT JOIN FETCH b.truSoDeXuat
        LEFT JOIN FETCH b.truSoTiepNhan
        WHERE b.trangThaiDuyet IN ('AI_APPROVED', 'VERIFIED')
        AND b.trangThaiXuLy NOT IN ('HOAN_THANH', 'HUY_BO')
    """)
    List<BaoCaoSuCo> findAllForMapEntity();

    @Query("""
    SELECT b FROM BaoCaoSuCo b
    LEFT JOIN FETCH b.loaiSuCo
    LEFT JOIN FETCH b.reporter
    WHERE b.trangThaiDuyet IN ('AI_APPROVED', 'PENDING')
    AND b.trangThaiXuLy = 'CHO_XU_LY'
    ORDER BY b.thoiGianTao DESC
""")
List<BaoCaoSuCo> findPendingReportsForAdmin();

    // =========================
    // 2. TRỤ SỞ MAP
    // =========================
    @Query("""
        SELECT DISTINCT b FROM BaoCaoSuCo b
        LEFT JOIN FETCH b.loaiSuCo
        LEFT JOIN FETCH b.reporter
        LEFT JOIN FETCH b.truSoDeXuat
        LEFT JOIN FETCH b.truSoTiepNhan
        WHERE (
            (b.trangThaiXuLy = 'CHO_XU_LY' AND b.truSoDeXuat.id = :idTruSo)
            OR
            (b.trangThaiXuLy = 'DANG_XU_LY' AND b.truSoTiepNhan.id = :idTruSo)
        )
        AND b.trangThaiDuyet != 'REJECTED'
        AND b.trangThaiXuLy != 'HUY_BO'
    """)
    List<BaoCaoSuCo> findActiveByTruSo(@Param("idTruSo") Long idTruSo);


    // =========================
    // 3. HISTORY
    // =========================
    @Query("""
        SELECT DISTINCT b FROM BaoCaoSuCo b
        LEFT JOIN FETCH b.loaiSuCo
        LEFT JOIN FETCH b.reporter
        WHERE b.trangThaiXuLy = 'HOAN_THANH'
        AND b.truSoTiepNhan.id = :idTruSo
    """)
    List<BaoCaoSuCo> findHistoryByTruSo(@Param("idTruSo") Long idTruSo);

List<BaoCaoSuCo> findByTrangThaiXuLyNotIn(List<String> statuses);
    // 4. USER
    // =========================
    List<BaoCaoSuCo> findByReporterUid(String uid);
}
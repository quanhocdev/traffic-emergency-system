package com.example.suco.repository.suco.baocao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.suco.model.BaoCaoSuCo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SuCoTruSoRepository
        extends JpaRepository<BaoCaoSuCo, Long> {

    // Chờ xử lý
    @Query("""
        SELECT DISTINCT b FROM BaoCaoSuCo b
        LEFT JOIN FETCH b.loaiSuCo
        LEFT JOIN FETCH b.reporter
        LEFT JOIN FETCH b.truSoTiepNhan
        WHERE b.truSoTiepNhan.id = :idTruSo
        AND b.trangThaiXuLy = 'CHO_XU_LY'
    """)
    List<BaoCaoSuCo> findPendingByTruSo(
            @Param("idTruSo") Long idTruSo
    );

    // Đang xử lý
    @Query("""
        SELECT DISTINCT b FROM BaoCaoSuCo b
        LEFT JOIN FETCH b.loaiSuCo
        LEFT JOIN FETCH b.reporter
        LEFT JOIN FETCH b.truSoTiepNhan
        WHERE b.truSoTiepNhan.id = :idTruSo
        AND b.trangThaiXuLy = 'DANG_XU_LY'
    """)
    List<BaoCaoSuCo> findActiveByTruSo(
            @Param("idTruSo") Long idTruSo
    );

    // Lịch sử
    @Query("""
        SELECT DISTINCT b FROM BaoCaoSuCo b
        LEFT JOIN FETCH b.loaiSuCo
        LEFT JOIN FETCH b.reporter
        LEFT JOIN FETCH b.truSoTiepNhan
        WHERE b.truSoTiepNhan.id = :idTruSo
        AND b.trangThaiXuLy = 'HOAN_THANH'
    """)
    List<BaoCaoSuCo> findHistoryByTruSo(
            @Param("idTruSo") Long idTruSo
    );
}
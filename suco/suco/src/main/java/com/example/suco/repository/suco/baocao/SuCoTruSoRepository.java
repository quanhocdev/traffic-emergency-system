package com.example.suco.repository.suco.baocao;

import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.model.enums.TrangThaiXuLy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SuCoTruSoRepository extends JpaRepository<BaoCaoSuCo, Long> {

    @Query("""
        SELECT DISTINCT b FROM BaoCaoSuCo b
        LEFT JOIN FETCH b.loaiSuCo
        LEFT JOIN FETCH b.reporter
        LEFT JOIN FETCH b.truSoTiepNhan
        WHERE b.truSoTiepNhan.id = :idTruSo
        AND b.trangThaiXuLy = :status
    """)
    List<BaoCaoSuCo> findByTruSoAndStatus(
            @Param("idTruSo") Long idTruSo,
            @Param("status") TrangThaiXuLy status
    );


default List<BaoCaoSuCo> findNewAssignedByTruSo(Long idTruSo) {
    return findByTruSoAndStatus(idTruSo, TrangThaiXuLy.DA_TIEP_NHAN);
}

    default List<BaoCaoSuCo> findPendingByTruSo(Long idTruSo) {
        return findByTruSoAndStatus(idTruSo, TrangThaiXuLy.DANG_DI_CHUYEN);
    }

    default List<BaoCaoSuCo> findActiveByTruSo(Long idTruSo) {
        return findByTruSoAndStatus(idTruSo, TrangThaiXuLy.DANG_XU_LY);
    }

    default List<BaoCaoSuCo> findHistoryByTruSo(Long idTruSo) {
        return findByTruSoAndStatus(idTruSo, TrangThaiXuLy.HOAN_THANH);
    }
}
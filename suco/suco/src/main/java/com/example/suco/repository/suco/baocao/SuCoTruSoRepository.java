package com.example.suco.repository.suco.baocao;

import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.model.enums.TrangThaiXuLy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SuCoTruSoRepository extends JpaRepository<BaoCaoSuCo, Long> {

    // =========================
    // CHỜ TRỤ SỞ NHẬN (DA_TIEP_NHAN / CHO_XU_LY)
    // =========================
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

    // =========================
// MỚI TIẾP NHẬN (AI vừa gán)
// =========================
default List<BaoCaoSuCo> findNewAssignedByTruSo(Long idTruSo) {
    return findByTruSoAndStatus(idTruSo, TrangThaiXuLy.DA_TIEP_NHAN);
}

    // =========================
    // CHỜ XỬ LÝ
    // =========================
    default List<BaoCaoSuCo> findPendingByTruSo(Long idTruSo) {
        return findByTruSoAndStatus(idTruSo, TrangThaiXuLy.DANG_DI_CHUYEN);
    }

    // =========================
    // ĐANG XỬ LÝ
    // =========================
    default List<BaoCaoSuCo> findActiveByTruSo(Long idTruSo) {
        return findByTruSoAndStatus(idTruSo, TrangThaiXuLy.DANG_XU_LY);
    }

    // =========================
    // HOÀN THÀNH
    // =========================
    default List<BaoCaoSuCo> findHistoryByTruSo(Long idTruSo) {
        return findByTruSoAndStatus(idTruSo, TrangThaiXuLy.HOAN_THANH);
    }
}
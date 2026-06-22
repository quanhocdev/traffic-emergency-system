package com.example.suco.repository.suco.baocao;

import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.model.enums.TrangThaiXuLy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SuCoAdminRepository extends JpaRepository<BaoCaoSuCo, Long> {

    // 1. Tối ưu hiệu năng, lấy đầy đủ thông tin liên kết và sắp xếp mới nhất lên đầu
    @Query("""
        SELECT DISTINCT b FROM BaoCaoSuCo b
        LEFT JOIN FETCH b.loaiSuCo
        LEFT JOIN FETCH b.reporter
        LEFT JOIN FETCH b.truSoTiepNhan
        WHERE b.trangThaiXuLy = :status
        ORDER BY b.id DESC
    """)
    List<BaoCaoSuCo> findByStatus(@Param("status") TrangThaiXuLy status);

    // 2. HÀM GỐC LẤY TẤT CẢ (Cho tab "Tất cả sự cố" của Admin hiển thị đủ mọi trạng thái)
    @Query("""
        SELECT DISTINCT b FROM BaoCaoSuCo b
        LEFT JOIN FETCH b.loaiSuCo
        LEFT JOIN FETCH b.reporter
        LEFT JOIN FETCH b.truSoTiepNhan
        ORDER BY b.id DESC
    """)
    List<BaoCaoSuCo> findAllForAdminDashboard();

    
    default List<BaoCaoSuCo> findNewAssigned() {
        return findByStatus(TrangThaiXuLy.DA_TIEP_NHAN);
    }

    default List<BaoCaoSuCo> findPending() {
        return findByStatus(TrangThaiXuLy.DANG_DI_CHUYEN);
    }

    default List<BaoCaoSuCo> findActive() {
        return findByStatus(TrangThaiXuLy.DANG_XU_LY);
    }

    default List<BaoCaoSuCo> findHistory() {
        return findByStatus(TrangThaiXuLy.HOAN_THANH);
    }

    default List<BaoCaoSuCo> findCancel() {
        return findByStatus(TrangThaiXuLy.HUY_BO);
    }

    List<BaoCaoSuCo> findByReporterUid(String uid);
}
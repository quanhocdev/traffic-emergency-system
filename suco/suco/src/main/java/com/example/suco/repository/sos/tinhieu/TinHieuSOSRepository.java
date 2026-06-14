package com.example.suco.repository.sos.tinhieu;

import com.example.suco.model.TinHieuSOS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TinHieuSOSRepository extends JpaRepository<TinHieuSOS, Long> {
    
    // Lấy danh sách SOS của riêng một User (phục vụ app di động của khách hàng)
    List<TinHieuSOS> findByUserUid(String uid);

    // Lấy tất cả SOS đang hoạt động, cho map
    @Query("""
    SELECT DISTINCT s FROM TinHieuSOS s 
    LEFT JOIN FETCH s.user u 
    WHERE s.idTruSoTiepNhan = :idTruSo
    AND s.trangThai IN ('DA_TIEP_NHAN','DANG_DI_CHUYEN','DANG_XU_LY', 'HOAN_THANH')
    ORDER BY s.createdAt DESC
""")
List<TinHieuSOS> findActiveSOSByTruSo(@Param("idTruSo") Long idTruSo);

// Lấy tất cả SOS cho từng trang
@Query("""
        SELECT DISTINCT s FROM TinHieuSOS s 
        LEFT JOIN FETCH s.user u 
        WHERE s.idTruSoTiepNhan = :idTruSo
        AND s.trangThai = :trangThai
        ORDER BY s.createdAt ASC
    """)
    List<TinHieuSOS> findByTruSoAndStatus(
            @Param("idTruSo") Long idTruSo, 
            @Param("trangThai") String trangThai
    );
    default List<TinHieuSOS> findNewAssignedByTruSo(Long idTruSo) {
        return findByTruSoAndStatus(idTruSo, "DA_TIEP_NHAN");
    }

    default List<TinHieuSOS> findMovingByTruSo(Long idTruSo) {
        return findByTruSoAndStatus(idTruSo, "DANG_DI_CHUYEN");
    }

    default List<TinHieuSOS> findActiveByTruSo(Long idTruSo) {
        return findByTruSoAndStatus(idTruSo, "DANG_XU_LY"); 
    }

    default List<TinHieuSOS> findHistoryByTruSo(Long idTruSo) {
        return findByTruSoAndStatus(idTruSo, "HOAN_THANH");
    }
}
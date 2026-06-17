package com.example.suco.repository.sos.tinhieu;

import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.enums.TrangThaiXuLy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TinHieuSOSRepository extends JpaRepository<TinHieuSOS, Long> {
    
    List<TinHieuSOS> findByUserUid(String uid);

    @Query("""
        SELECT DISTINCT s FROM TinHieuSOS s 
        LEFT JOIN FETCH s.user u 
        WHERE s.idTruSoTiepNhan = :idTruSo
        AND s.trangThai IN :statuses
        ORDER BY s.createdAt DESC
    """)
    List<TinHieuSOS> findActiveSOSByStatuses(
            @Param("idTruSo") Long idTruSo,
            @Param("statuses") List<TrangThaiXuLy> statuses
    );

    default List<TinHieuSOS> findActiveSOSByTruSo(Long idTruSo) {
        return findActiveSOSByStatuses(idTruSo, List.of(
            TrangThaiXuLy.DA_TIEP_NHAN,
            TrangThaiXuLy.DANG_DI_CHUYEN,
            TrangThaiXuLy.DANG_XU_LY
        ));
    }

    @Query("""
        SELECT DISTINCT s FROM TinHieuSOS s 
        LEFT JOIN FETCH s.user u 
        WHERE s.idTruSoTiepNhan = :idTruSo
        AND s.trangThai = :trangThai
        ORDER BY s.createdAt ASC
    """)
    List<TinHieuSOS> findByTruSoAndStatus(
            @Param("idTruSo") Long idTruSo, 
            @Param("trangThai") TrangThaiXuLy trangThai
    );
    
    default List<TinHieuSOS> findNewAssignedByTruSo(Long idTruSo) {
        return findByTruSoAndStatus(idTruSo, TrangThaiXuLy.DA_TIEP_NHAN);
    }

    default List<TinHieuSOS> findMovingByTruSo(Long idTruSo) {
        return findByTruSoAndStatus(idTruSo, TrangThaiXuLy.DANG_DI_CHUYEN);
    }

    default List<TinHieuSOS> findActiveByTruSo(Long idTruSo) {
        return findByTruSoAndStatus(idTruSo, TrangThaiXuLy.DANG_XU_LY); 
    }

    default List<TinHieuSOS> findHistoryByTruSo(Long idTruSo) {
        return findByTruSoAndStatus(idTruSo, TrangThaiXuLy.HOAN_THANH);
    }
    default List<TinHieuSOS> findCancelByTruSo(Long idTruSo) {
        return findByTruSoAndStatus(idTruSo, TrangThaiXuLy.HUY_BO);
    }
}
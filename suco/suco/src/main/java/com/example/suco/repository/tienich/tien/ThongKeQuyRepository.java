package com.example.suco.repository.tienich.tien;


import com.example.suco.dto.tienich.tien.quanly.VinhDanhDTO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.suco.model.QuyenGop;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface ThongKeQuyRepository 
        extends JpaRepository<QuyenGop, Long> {



    // Tổng quỹ
    @Query("""
        SELECT COALESCE(SUM(q.giaTri),0)
        FROM QuyenGop q
    """)
    Long sumTongQuyenGop();




    // Tổng quỹ theo khoảng thời gian
    @Query("""
        SELECT COALESCE(SUM(q.giaTri),0)
        FROM QuyenGop q
        WHERE q.ngayQuyenGop BETWEEN :start AND :end
    """)
    Long sumQuyenGopTheoThoiGian(
            LocalDateTime start,
            LocalDateTime end
    );




    // Bảng vinh danh tất cả
    @Query("""
        SELECT new com.example.suco.dto.tienich.tien.quanly.VinhDanhDTO(
            q.user.name,
            SUM(q.giaTri)
        )
        FROM QuyenGop q
        GROUP BY q.user.uid, q.user.name
        ORDER BY SUM(q.giaTri) DESC
    """)
    List<VinhDanhDTO> findBangVinhDanh();




    // Bảng vinh danh theo ngày/tháng
    @Query("""
        SELECT new com.example.suco.dto.tienich.tien.quanly.VinhDanhDTO(
            q.user.name,
            SUM(q.giaTri)
        )
        FROM QuyenGop q
        WHERE q.ngayQuyenGop BETWEEN :start AND :end
        GROUP BY q.user.uid, q.user.name
        ORDER BY SUM(q.giaTri) DESC
    """)
    List<VinhDanhDTO> findBangVinhDanhTheoThoiGian(
            LocalDateTime start,
            LocalDateTime end
    );

}
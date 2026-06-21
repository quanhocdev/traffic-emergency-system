package com.example.suco.repository.sos.tinhieu;

import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.enums.TrangThaiXuLy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AdminTinHieuSOSRepository extends JpaRepository<TinHieuSOS, Long> {

    // Hàm gốc/Hàm tổng quát: Lọc danh sách tín hiệu toàn hệ thống theo một trạng thái bất kỳ
    @Query("""
        SELECT DISTINCT s FROM TinHieuSOS s 
        LEFT JOIN FETCH s.user u 
        WHERE s.trangThai = :trangThai
        ORDER BY s.createdAt DESC
    """)
    List<TinHieuSOS> findAdminByStatus(@Param("trangThai") TrangThaiXuLy trangThai);

    // --- CÁC HÀM DEFAULT GỌI THEO TRẠNG THÁI (Tương tự như phía trụ sở) ---

    // 1. Admin lấy các tín hiệu Mới tiếp nhận trên toàn hệ thống
    default List<TinHieuSOS> findAdminNewAssigned() {
        return findAdminByStatus(TrangThaiXuLy.DA_TIEP_NHAN);
    }

    // 2. Admin lấy các tín hiệu Đang di chuyển trên toàn hệ thống
    default List<TinHieuSOS> findAdminMoving() {
        return findAdminByStatus(TrangThaiXuLy.DANG_DI_CHUYEN);
    }

    // 3. Admin lấy các tín hiệu Đang xử lý trên toàn hệ thống
    default List<TinHieuSOS> findAdminActive() {
        return findAdminByStatus(TrangThaiXuLy.DANG_XU_LY);
    }

    // 4. Admin lấy Lịch sử các tín hiệu đã Hoàn thành
    default List<TinHieuSOS> findAdminHistory() {
        return findAdminByStatus(TrangThaiXuLy.HOAN_THANH);
    }

    // 5. Admin lấy các tín hiệu Đã hủy bỏ
    default List<TinHieuSOS> findAdminCancel() {
        return findAdminByStatus(TrangThaiXuLy.HUY_BO);
    }

    // --- HÀM BỔ SUNG: Lấy tất cả tín hiệu đang mở/cần xử lý (Tổng hợp) ---
    @Query("""
        SELECT DISTINCT s FROM TinHieuSOS s 
        LEFT JOIN FETCH s.user u 
        WHERE s.trangThai IN :statuses
        ORDER BY s.createdAt DESC
    """)
    List<TinHieuSOS> findAdminActiveSOSByStatuses(@Param("statuses") List<TrangThaiXuLy> statuses);

    default List<TinHieuSOS> findAdminAllActive() {
        return findAdminActiveSOSByStatuses(List.of(
            TrangThaiXuLy.DA_TIEP_NHAN,
            TrangThaiXuLy.DANG_DI_CHUYEN,
            TrangThaiXuLy.DANG_XU_LY
        ));
    }
}
package com.example.suco.repository.payment;

import com.example.suco.model.ThanhToanHoaDon;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ThanhToanCuuHoRepository
        extends JpaRepository<ThanhToanHoaDon, Long> {

    // Lấy tất cả transaction của hóa đơn
    List<ThanhToanHoaDon> findByHoaDonIdOrderByIdDesc(Long hoaDonId);

    // Lấy transaction mới nhất
    Optional<ThanhToanHoaDon>
        findFirstByHoaDonIdOrderByIdDesc(Long hoaDonId);

    // Lọc theo trạng thái
    List<ThanhToanHoaDon>
        findByTrangThai(String trangThai);

    // Kiểm tra đã thanh toán thành công chưa
    boolean existsByHoaDonIdAndTrangThai(
        Long hoaDonId,
        String trangThai
    );
}
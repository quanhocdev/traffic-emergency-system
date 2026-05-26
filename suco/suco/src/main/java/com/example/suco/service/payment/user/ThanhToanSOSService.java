package com.example.suco.service.payment.user;

import com.example.suco.model.*;
import com.example.suco.repository.*;
import com.example.suco.repository.payment.HoaDonRepository;
import com.example.suco.repository.sos.goi.admin.CRUDGoiRepository;
import com.example.suco.repository.sos.goi.user.SoHuuGoiRepository;
import com.example.suco.repository.sos.tinhieu.TinHieuSOSRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ThanhToanSOSService {
    @Autowired private HoaDonRepository hoaDonRepository;
    @Autowired private SoHuuGoiRepository muaGoiRepository;
    @Autowired private CRUDGoiRepository goiRepository;
    @Autowired private TinHieuSOSRepository tinHieuSOSRepository;
    @Autowired private TruSoRepository truSoRepository;
    @Autowired private DoiQuaRepository doiQuaRepository;
    @Autowired private QuaRepository quaRepository;

    
    @Transactional
public void apDungVoucherChoHoaDon(HoaDon hd, Long quaId) {
    // 1. Lấy thông tin Voucher từ bảng 'qua'
    Qua voucher = quaRepository.findById(quaId)
            .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));

    if (voucher.getLoai() != Qua.LoaiQua.VOUCHER) {
        throw new RuntimeException("Vật phẩm này không phải là Voucher");
    }

    // 2. Tính toán lại số tiền giảm
    BigDecimal giaGoc = hd.getThanhTien();
    BigDecimal phanTram = BigDecimal.valueOf(voucher.getGiaTriGiamPercent())
            .divide(new BigDecimal(100));
    BigDecimal soTienGiam = giaGoc.multiply(phanTram);

    // Kiểm tra trần giảm giá (nếu có)
    if (voucher.getGiaTriToiDa() != null && soTienGiam.compareTo(voucher.getGiaTriToiDa()) > 0) {
        soTienGiam = voucher.getGiaTriToiDa();
    }

    // 3. Cập nhật thông tin vào hóa đơn
    hd.setQuaId(quaId);
    hd.setSoTienGiam(soTienGiam);
    
    BigDecimal tongMoi = giaGoc.subtract(soTienGiam);
    if (tongMoi.compareTo(BigDecimal.ZERO) < 0) tongMoi = BigDecimal.ZERO;
    hd.setTongThanhToan(tongMoi);

    // 4. Thực hiện trừ số lượng Voucher trong túi của người dùng
    doiQuaRepository.findByUserIdAndQuaId(hd.getUserId(), quaId).ifPresent(dq -> {
        if (dq.getSoLuong() != null && dq.getSoLuong() > 1) {
            dq.setSoLuong(dq.getSoLuong() - 1);
            doiQuaRepository.save(dq);
        } else {
            doiQuaRepository.delete(dq);
        }
    });
    
    // Lưu lại hóa đơn đã cập nhật số tiền
    hoaDonRepository.save(hd);
}
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // KM
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon/2) * Math.sin(dLon/2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }
}
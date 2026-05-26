package com.example.suco.service.sos.payment.hoadon.user;

import com.example.suco.model.*;
import com.example.suco.repository.*;
import com.example.suco.repository.payment.HoaDonRepository;
import com.example.suco.dto.sos.payment.hoadon.request.ThanhToanRequestDTO;
import com.example.suco.dto.sos.payment.hoadon.response.ThanhToanResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ThanhToanSOSService {
    @Autowired private HoaDonRepository hoaDonRepository;
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
@Transactional
public ThanhToanResponseDTO thanhToanHoaDon(
        String uid,
        ThanhToanRequestDTO request
) {

    HoaDon hd = hoaDonRepository.findById(request.getHoaDonId())
            .orElseThrow(() ->
                    new RuntimeException("Không tìm thấy hóa đơn")
            );

    // Check chính chủ
    if (hd.getUserId() == null || !hd.getUserId().equals(uid)) {
        throw new RuntimeException(
                "Bạn không có quyền thanh toán hóa đơn này"
        );
    }

    // Tránh thanh toán lại
    if ("PAID".equalsIgnoreCase(hd.getTrangThai())) {
        throw new RuntimeException(
                "Hóa đơn đã được thanh toán trước đó"
        );
    }

    // Áp voucher nếu có
    if (request.getQuaId() != null) {
        apDungVoucherChoHoaDon(hd, request.getQuaId());
    }

    // Update trạng thái
    hd.setTrangThai("PAID");

    hoaDonRepository.save(hd);

    // Response
    ThanhToanResponseDTO response =
            new ThanhToanResponseDTO();

    response.setHoaDonId(hd.getId());
    response.setTrusoId(hd.getTrusoId());
    response.setTrangThai(hd.getTrangThai());
    response.setTongThanhToan(hd.getTongThanhToan());
    response.setMessage("Thanh toán thành công");

    return response;
}
    
}
package com.example.suco.service.sos.payment.hoadon.user;

import com.example.suco.dto.sos.payment.thanhtoancuuho.ThanhToanRequestDTO;
import com.example.suco.dto.sos.payment.thanhtoancuuho.ThanhToanResponseDTO;
import com.example.suco.model.*;
import com.example.suco.repository.*;
import com.example.suco.repository.payment.HoaDonRepository;
import com.example.suco.repository.payment.ThanhToanHoaDonRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ThanhToanSOSService {
    @Autowired private HoaDonRepository hoaDonRepository;
    @Autowired private DoiQuaRepository doiQuaRepository;
    @Autowired private QuaRepository quaRepository;
@Autowired
private ThanhToanHoaDonRepository thanhToanHoaDonRepository;
    
    @Transactional
public BigDecimal apDungVoucher(
        HoaDon hd,
        Long quaId
) {

    Qua voucher = quaRepository.findById(quaId)
            .orElseThrow(() ->
                    new RuntimeException("Voucher không tồn tại")
            );

    if (voucher.getLoai() != Qua.LoaiQua.VOUCHER) {
        throw new RuntimeException(
                "Vật phẩm này không phải Voucher"
        );
    }

    BigDecimal giaGoc = hd.getThanhTien();

    BigDecimal phanTram =
            BigDecimal.valueOf(voucher.getGiaTriGiamPercent())
                    .divide(new BigDecimal(100));

    BigDecimal soTienGiam =
            giaGoc.multiply(phanTram);

    if (
            voucher.getGiaTriToiDa() != null
            &&
            soTienGiam.compareTo(voucher.getGiaTriToiDa()) > 0
    ) {
        soTienGiam = voucher.getGiaTriToiDa();
    }

    // Trừ kho voucher user
    doiQuaRepository
            .findByUserIdAndQuaId(
                    hd.getUserId(),
                    quaId
            )
            .ifPresent(dq -> {

                if (
                        dq.getSoLuong() != null
                        &&
                        dq.getSoLuong() > 1
                ) {

                    dq.setSoLuong(
                            dq.getSoLuong() - 1
                    );

                    doiQuaRepository.save(dq);

                } else {

                    doiQuaRepository.delete(dq);
                }
            });

    return soTienGiam;
}
@Transactional
public ThanhToanResponseDTO thanhToanHoaDon(
        String uid,
        ThanhToanRequestDTO request
) {

    HoaDon hd = hoaDonRepository
            .findById(request.getHoaDonId())
            .orElseThrow(() ->
                    new RuntimeException(
                            "Không tìm thấy hóa đơn"
                    )
            );

    // Check chủ hóa đơn
    if (
            hd.getUserId() == null
            ||
            !hd.getUserId().equals(uid)
    ) {

        throw new RuntimeException(
                "Bạn không có quyền thanh toán hóa đơn này"
        );
    }

    // Giá gốc
    BigDecimal thanhTien =
            hd.getThanhTien();

    // Giảm giá
    BigDecimal soTienGiam =
            BigDecimal.ZERO;

    if (request.getQuaId() != null) {

        soTienGiam = apDungVoucher(
                hd,
                request.getQuaId()
        );
    }

    // Tổng cuối
    BigDecimal tongThanhToan =
            thanhTien.subtract(soTienGiam);

    if (
            tongThanhToan.compareTo(BigDecimal.ZERO)
                    < 0
    ) {

        tongThanhToan = BigDecimal.ZERO;
    }

    // Tạo payment transaction
    ThanhToanHoaDon thanhToan =
            new ThanhToanHoaDon();

    thanhToan.setHoaDon(hd);

    thanhToan.setPhuongThucThanhToan(
            request.getPhuongThucThanhToan()
    );

    thanhToan.setTrangThai("SUCCESS");

    thanhToan.setThanhTien(thanhTien);

    thanhToan.setSoTienGiam(soTienGiam);

    thanhToan.setTongThanhToan(tongThanhToan);

    thanhToanHoaDonRepository.save(thanhToan);

    // Response
    ThanhToanResponseDTO response =
            new ThanhToanResponseDTO();

    response.setThanhToanId(
            thanhToan.getId()
    );

    response.setHoaDonId(hd.getId());

    response.setTrusoId(hd.getTrusoId());

    response.setPhuongThucThanhToan(
            thanhToan.getPhuongThucThanhToan()
    );

    response.setTrangThai(
            thanhToan.getTrangThai()
    );

    response.setThanhTien(
            thanhToan.getThanhTien()
    );

    response.setSoTienGiam(
            thanhToan.getSoTienGiam()
    );

    response.setTongThanhToan(
            thanhToan.getTongThanhToan()
    );

    response.setMessage(
            "Thanh toán thành công"
    );

    return response;
}
}
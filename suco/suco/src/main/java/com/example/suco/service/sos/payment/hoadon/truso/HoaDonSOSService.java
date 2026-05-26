package com.example.suco.service.sos.payment.hoadon.truso;

import com.example.suco.dto.sos.payment.hoadon.request.HoaDonRequestDTO;
import com.example.suco.dto.sos.payment.hoadon.response.HoaDonResponseDTO;
import com.example.suco.model.*;
import com.example.suco.repository.*;
import com.example.suco.repository.payment.HoaDonRepository;
import com.example.suco.repository.sos.goi.admin.CRUDGoiRepository;
import com.example.suco.repository.sos.goi.user.SoHuuGoiRepository;
import com.example.suco.repository.sos.tinhieu.TinHieuSOSRepository;
import com.example.suco.service.sos.payment.hoadon.truso.validation.DistanceService;
import com.example.suco.dto.sos.payment.hoadon.request.HoaDonRequestDTO;
import com.example.suco.dto.sos.payment.hoadon.response.HoaDonResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HoaDonSOSService {
    @Autowired private HoaDonRepository hoaDonRepository;
    @Autowired private SoHuuGoiRepository muaGoiRepository;
    @Autowired private CRUDGoiRepository goiRepository;
    @Autowired private TinHieuSOSRepository tinHieuSOSRepository;
    @Autowired private TruSoRepository truSoRepository;
    @Autowired private DoiQuaRepository doiQuaRepository;
    @Autowired private QuaRepository quaRepository;
    @Autowired private DistanceService distanceService;

   @Transactional
public HoaDonResponseDTO taoHoaDon(
        HoaDonRequestDTO request,
        Long trusoId
) {

    TinHieuSOS sos = tinHieuSOSRepository.findById(request.getSosId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy SOS"));

    TruSo truso = truSoRepository.findById(trusoId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy Trụ sở"));

    if (sos.getIdTruSoTiepNhan() == null) {
        throw new RuntimeException("SOS chưa được tiếp nhận");
    }

    if (!sos.getIdTruSoTiepNhan().equals(trusoId)) {
        throw new RuntimeException("SOS này không thuộc trụ sở của bạn");
    }

    if (!"DANG_XU_LY".equals(sos.getTrangThai())) {
        throw new RuntimeException("Chỉ tạo hóa đơn khi SOS đang xử lý");
    }

    HoaDon hd = new HoaDon();

    hd.setSosId(request.getSosId());
    hd.setTrusoId(trusoId);
    hd.setUserId(sos.getUserId());
    hd.setTenSos(request.getTenSos());
    hd.setNoiDungXuLy(request.getNoiDungXuLy());

    BigDecimal giaGoc = BigDecimal.ZERO;

    Optional<MuaGoi> muaGoiOpt =
            muaGoiRepository.findFirstByUserIdAndTrangThai(
                    sos.getUserId(),
                    "ACTIVE"
            );

    if (muaGoiOpt.isPresent()) {

        Goi goi = goiRepository.findById(
                muaGoiOpt.get().getGoiId()
        ).get();

        double distance = distanceService.calculateDistance(
                truso.getViDo(),
                truso.getKinhDo(),
                sos.getViDo(),
                sos.getKinhDo()
        );

        double freeKm =
                (goi.getKhoangCachMienPhi() != null)
                        ? goi.getKhoangCachMienPhi()
                        : 0;

        double extraKm = Math.max(0, distance - freeKm);

        long soKmTinhTien = (long) Math.ceil(extraKm);

        double finalPrice = soKmTinhTien * 10000;

        giaGoc = BigDecimal.valueOf(finalPrice);

    } else {

        giaGoc = BigDecimal.valueOf(
                request.getGiaThuCong() != null
                        ? request.getGiaThuCong()
                        : 0
        );
    }

    hd.setThanhTien(giaGoc);

    BigDecimal soTienGiam = BigDecimal.ZERO;

    if (request.getQuaId() != null) {

        Optional<Qua> quaOpt =
                quaRepository.findById(request.getQuaId());

        if (quaOpt.isPresent()
                && quaOpt.get().getLoai() == Qua.LoaiQua.VOUCHER) {

            Qua v = quaOpt.get();

            hd.setQuaId(request.getQuaId());

            BigDecimal phanTram =
                    BigDecimal.valueOf(v.getGiaTriGiamPercent())
                            .divide(new BigDecimal(100));

            soTienGiam = giaGoc.multiply(phanTram);

            if (v.getGiaTriToiDa() != null
                    && soTienGiam.compareTo(v.getGiaTriToiDa()) > 0) {

                soTienGiam = v.getGiaTriToiDa();
            }
        }
    }

    hd.setSoTienGiam(soTienGiam);

    BigDecimal thucTra = giaGoc.subtract(soTienGiam);

    if (thucTra.compareTo(BigDecimal.ZERO) < 0) {
        thucTra = BigDecimal.ZERO;
    }

    hd.setTongThanhToan(thucTra);

    hd.setTrangThai(
            thucTra.compareTo(BigDecimal.ZERO) == 0
                    ? "PAID"
                    : "PENDING"
    );

    HoaDon savedHd = hoaDonRepository.save(hd);

    sos.setHoaDon(savedHd);

    tinHieuSOSRepository.save(sos);

    if (request.getQuaId() != null) {

        doiQuaRepository.findByUserIdAndQuaId(
                savedHd.getUserId(),
                request.getQuaId()
        ).ifPresent(dq -> {

            if (dq.getSoLuong() != null && dq.getSoLuong() > 1) {

                dq.setSoLuong(dq.getSoLuong() - 1);

                doiQuaRepository.save(dq);

            } else {

                doiQuaRepository.delete(dq);
            }
        });
    }

    HoaDonResponseDTO response = new HoaDonResponseDTO();

    response.setId(savedHd.getId());
    response.setSosId(savedHd.getSosId());
    response.setTrusoId(savedHd.getTrusoId());
    response.setUserId(savedHd.getUserId());
    response.setTenSos(savedHd.getTenSos());
    response.setNoiDungXuLy(savedHd.getNoiDungXuLy());
    response.setThanhTien(savedHd.getThanhTien());
    response.setQuaId(savedHd.getQuaId());
    response.setSoTienGiam(savedHd.getSoTienGiam());
    response.setTongThanhToan(savedHd.getTongThanhToan());
    response.setTrangThai(savedHd.getTrangThai());
    response.setCreatedAt(savedHd.getCreatedAt());

    return response;
}

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
}
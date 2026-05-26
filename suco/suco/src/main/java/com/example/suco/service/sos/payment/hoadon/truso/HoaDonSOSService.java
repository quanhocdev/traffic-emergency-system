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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class HoaDonSOSService {

    @Autowired private HoaDonRepository hoaDonRepository;
    @Autowired private SoHuuGoiRepository muaGoiRepository;
    @Autowired private CRUDGoiRepository goiRepository;
    @Autowired private TinHieuSOSRepository tinHieuSOSRepository;
    @Autowired private TruSoRepository truSoRepository;
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

        BigDecimal giaGoc;

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

        // Chưa áp voucher lúc tạo hóa đơn
        hd.setSoTienGiam(BigDecimal.ZERO);

        // Ban đầu tổng thanh toán = giá gốc
        hd.setTongThanhToan(giaGoc);

        // Chưa thanh toán
        hd.setTrangThai("PENDING");

        HoaDon savedHd = hoaDonRepository.save(hd);

        sos.setHoaDon(savedHd);

        tinHieuSOSRepository.save(sos);

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
}
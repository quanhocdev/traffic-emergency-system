package com.example.suco.service.sos.hoadon.user;

import com.example.suco.dto.sos.hoadon.payment.ThanhToanRequestDTO;
import com.example.suco.dto.sos.hoadon.payment.ThanhToanResponseDTO;
import com.example.suco.mapper.ThanhToanCuuHoMapper;

import com.example.suco.model.HoaDon;
import com.example.suco.model.ThanhToanHoaDon;
import com.example.suco.repository.sos.hoadon.HoaDonCuuHoRepository;
import com.example.suco.repository.sos.hoadon.ThanhToanHoaDonRepository;
import com.example.suco.service.sos.hoadon.user.validation.StatusUserService;
import com.example.suco.service.sos.hoadon.user.validation.VoucherService;
import com.example.suco.service.sos.hoadon.user.validation.total.TotalAmountService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ThanhToanCuuHoService {

    @Autowired
    private HoaDonCuuHoRepository hoaDonRepository;

    @Autowired
    private ThanhToanHoaDonRepository thanhToanHoaDonRepository;

    @Autowired
    private VoucherService voucherService;

    @Autowired
    private StatusUserService statusService;

    @Autowired
    private TotalAmountService totalAmountService;

    @Autowired
    private ThanhToanCuuHoMapper mapper;

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

        // Validate
        statusService.validateHoaDon(
                hd,
                uid
        );

        // Giá gốc
        BigDecimal thanhTien =
                hd.getThanhTien();

        // Giảm giá
        BigDecimal soTienGiam =
                voucherService.apDungVoucher(
                        hd,
                        request.getQuaId()
                );

        // Tổng cuối
        BigDecimal tongThanhToan =
                totalAmountService.tinhTongTien(
                        thanhTien,
                        soTienGiam
                );

        // Mapper Request -> Entity
        ThanhToanHoaDon thanhToan =
                mapper.toEntity(
                        hd,
                        request,
                        thanhTien,
                        soTienGiam,
                        tongThanhToan
                );

        // Save
        thanhToanHoaDonRepository.save(
                thanhToan
        );

        // Entity -> Response
        return mapper.toDTO(
                thanhToan
        );
    }
}
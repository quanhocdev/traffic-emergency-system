package com.example.suco.service.sos.payment.hoadon.truso;

import com.example.suco.dto.sos.payment.hoadon.request.HoaDonRequestDTO;
import com.example.suco.dto.sos.payment.hoadon.response.HoaDonResponseDTO;
import com.example.suco.model.*;
import com.example.suco.repository.*;
import com.example.suco.repository.payment.HoaDonRepository;
import com.example.suco.repository.sos.tinhieu.TinHieuSOSRepository;
import com.example.suco.service.sos.payment.hoadon.truso.validation.VipSOSService;
import com.example.suco.service.sos.payment.hoadon.truso.total.TotalAmountService;
import com.example.suco.mapper.HoaDonMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.suco.service.sos.payment.hoadon.truso.validation.StatusService;
import java.math.BigDecimal;
import java.util.Optional;

@Service
public class HoaDonSOSService {

    @Autowired private HoaDonRepository hoaDonRepository;
    @Autowired private TinHieuSOSRepository tinHieuSOSRepository;
    @Autowired private TruSoRepository truSoRepository;
        @Autowired private VipSOSService vipSOSService;
@Autowired private HoaDonMapper hoaDonMapper;
@Autowired private TotalAmountService totalAmountService;
@Autowired private StatusService statusService;


    @Transactional
public HoaDonResponseDTO taoHoaDon(
        HoaDonRequestDTO req,
        Long trusoId
) {

    TinHieuSOS sos = tinHieuSOSRepository.findById(req.getSosId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy SOS"));

    TruSo truso = truSoRepository.findById(trusoId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy Trụ sở"));

    statusService.validate(sos, trusoId);

    Optional<Goi> goiOpt =
            vipSOSService.getActiveGoi(sos.getUserId());

    BigDecimal gia;

    if (goiOpt.isPresent()) {
        gia = totalAmountService.calculate(goiOpt.get(), truso, sos);
    } else {
        gia = BigDecimal.valueOf(
                req.getGiaThuCong() != null ? req.getGiaThuCong() : 0
        );
    }

    HoaDon hd = hoaDonMapper.toEntity(req, trusoId, sos.getUserId(), gia);

    HoaDon saved = hoaDonRepository.save(hd);

    sos.setHoaDon(saved);
    
    tinHieuSOSRepository.save(sos);

    return hoaDonMapper.toDTO(saved);
}

}
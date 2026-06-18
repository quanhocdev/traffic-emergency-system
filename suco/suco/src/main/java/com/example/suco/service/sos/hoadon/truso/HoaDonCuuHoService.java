package com.example.suco.service.sos.hoadon.truso;
import com.example.suco.model.*;
import com.example.suco.repository.sos.hoadon.HoaDonCuuHoRepository;
import com.example.suco.repository.sos.hoadon.ThanhToanHoaDonRepository;
import com.example.suco.repository.sos.tinhieu.TinHieuSOSRepository;
import com.example.suco.repository.vanhanh.TruSoRepository;
import com.example.suco.repository.sos.hoadon.ThanhToanHoaDonRepository; 
import com.example.suco.service.sos.hoadon.truso.validation.StatusTruSoService;
import com.example.suco.service.sos.hoadon.truso.validation.VipSOSService;
import com.example.suco.service.sos.hoadon.truso.validation.total.TotalService;
import com.example.suco.dto.sos.hoadon.quanly.HoaDonRequestDTO;
import com.example.suco.dto.sos.hoadon.quanly.HoaDonTruSoResponseDTO;
import com.example.suco.dto.sos.hoadon.quanly.HoaDonDetailDTO; 
import com.example.suco.dto.sos.hoadon.quanly.HoaDonResponseDTO; 
import com.example.suco.dto.sos.hoadon.payment.ThanhToanResponseDTO; 
import com.example.suco.mapper.HoaDonCuuHoMapper;
import com.example.suco.mapper.ThanhToanCuuHoMapper; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
@Service
public class HoaDonCuuHoService {

    @Autowired private HoaDonCuuHoRepository hoaDonRepository;
    @Autowired private TinHieuSOSRepository tinHieuSOSRepository;
    @Autowired private TruSoRepository truSoRepository;
    @Autowired private VipSOSService vipSOSService;
    @Autowired private HoaDonCuuHoMapper hoaDonMapper;
    @Autowired private TotalService totalAmountService;
    @Autowired private StatusTruSoService statusService;

    @Autowired private ThanhToanHoaDonRepository thanhToanHoaDonRepository;
    @Autowired private ThanhToanCuuHoMapper thanhToanCuuHoMapper;

    @Transactional
    public HoaDonTruSoResponseDTO taoHoaDon(HoaDonRequestDTO req, Long trusoId) {

        // 1. Kiểm tra sự tồn tại của Tín hiệu SOS
        TinHieuSOS sos = tinHieuSOSRepository.findById(req.getSosId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu SOS khẩn cấp (#" + req.getSosId() + ")"));

        // 2. Kiểm tra sự tồn tại của Trụ sở tiếp nhận
        TruSo truso = truSoRepository.findById(trusoId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin Trụ sở xử lý"));

        // 3. Thực hiện validate nghiệp vụ (Trụ sở có quyền xử lý ca SOS này không, trạng thái hợp lệ không...)
        statusService.validate(sos, trusoId);

        // 4. Kiểm tra gói cứu trợ cứu hộ cứu nạn (Goi) của User để tính toán giá tiền cụ thể
        Optional<Goi> goiOpt = vipSOSService.getActiveGoi(sos.getUserId());
        BigDecimal gia;

        if (goiOpt.isPresent()) {
            // Khách có gói cứu hộ ACTIVE -> Tính toán giá tiền tự động (có thể giảm giá hoặc miễn phí tùy gói)
            gia = totalAmountService.calculate(goiOpt.get(), truso, sos);
        } else {
            // Khách vãng lai / Khách thường -> Lấy giá thủ công nhập từ form Trụ sở gửi lên
            gia = BigDecimal.valueOf(req.getGiaThuCong() != null ? req.getGiaThuCong() : 0);
        }

        HoaDon hd = hoaDonMapper.toEntity(req, trusoId, sos.getUserId(), gia);
        HoaDon saved = hoaDonRepository.save(hd);

        sos.setHoaDon(saved);
        

        // Lưu  nhật tín hiệu SOS
        tinHieuSOSRepository.save(sos);

        return hoaDonMapper.toTruSoDTO(saved);
    }
    public HoaDonDetailDTO layChiTietTongHop(Long hoaDonId) {
    // 1. Tìm hóa đơn trong DB
    HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + hoaDonId));

    // 2. Map sang HoaDonResponseDTO
    HoaDonResponseDTO hdDto = hoaDonMapper.toDTO(hoaDon);

    // 3. Gọi đúng hàm lấy tất cả transaction đã sắp xếp Descending từ repo của ông
    List<ThanhToanHoaDon> danhSachThanhToan = thanhToanHoaDonRepository.findByHoaDonIdOrderByIdDesc(hoaDonId);

    // 4. Map danh sách sang DTO
    List<ThanhToanResponseDTO> listThanhToanDto = danhSachThanhToan.stream()
            .map(thanhToanCuuHoMapper::toDTO)
            .toList();

    // 5. Đóng gói vào DTO tổng hợp
    HoaDonDetailDTO detailDTO = new HoaDonDetailDTO();
    detailDTO.setHoaDon(hdDto);
    detailDTO.setThanhToans(listThanhToanDto);

    return detailDTO;
}
}
package com.example.suco.service.tienich.tien.admin;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.suco.dto.sos.hoadon.payment.ThanhToanResponseDTO;
import com.example.suco.dto.sos.hoadon.quanly.HoaDonResponseDTO;
import com.example.suco.dto.sos.hoadon.quanly.HoaDonDetailDTO;
import com.example.suco.mapper.hoadon.HoaDonCuuHoMapper;
import com.example.suco.mapper.hoadon.ThanhToanCuuHoMapper;
import com.example.suco.model.HoaDon;
import com.example.suco.repository.tienich.tien.DoanhThuRepository;
import com.example.suco.repository.sos.hoadon.HoaDonCuuHoRepository;
import com.example.suco.repository.sos.hoadon.ThanhToanHoaDonRepository;

@Service
public class DoanhThuService {

    @Autowired
    private DoanhThuRepository doanhThuRepository;

    @Autowired
    private HoaDonCuuHoRepository hoaDonRepository;

    @Autowired
    private HoaDonCuuHoMapper hoaDonCuuHoMapper;

    @Autowired
    private ThanhToanCuuHoMapper thanhToanCuuHoMapper;

    public BigDecimal layTongDoanhThu() {
        return doanhThuRepository.getTongDoanhThu();
    }

    public List<HoaDonResponseDTO> layDanhSachDoanhThu() {

    List<HoaDon> list = hoaDonRepository.findAllWithPayments();

    return list.stream().map(hd -> {

        boolean hasSuccess = hd.getThanhToans()
                .stream()
                .anyMatch(t -> "SUCCESS".equals(t.getTrangThai()));

        boolean hasPending = hd.getThanhToans()
                .stream()
                .anyMatch(t -> "PENDING".equals(t.getTrangThai()));

        String trangThai;

        if (hasSuccess) {
            trangThai = "PAID";
        } else if (hasPending) {
            trangThai = "PENDING";
        } else {
            trangThai = "FAILED";
        }

        HoaDonResponseDTO dto = new HoaDonResponseDTO();

        dto.setId(hd.getId());
        dto.setSosId(hd.getSosId());
        dto.setTrusoId(hd.getTrusoId());
        dto.setUserId(hd.getUserId());
        dto.setNoiDungXuLy(hd.getNoiDungXuLy());
        dto.setThanhTien(hd.getThanhTien());
        dto.setCreatedAt(hd.getCreatedAt());

        dto.setTrangThai(trangThai);

        return dto;

    }).toList();
}

    public HoaDonDetailDTO getChiTietHoaDon(Long hoaDonId) {

    HoaDon hd = hoaDonRepository.findDetailById(hoaDonId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

    HoaDonResponseDTO hoaDonDTO =
            hoaDonCuuHoMapper.toDTO(hd);

    List<ThanhToanResponseDTO> thanhToanDTOs =
            hd.getThanhToans().stream()
                    .map(thanhToanCuuHoMapper::toDTO)
                    .toList();

    HoaDonDetailDTO dto = new HoaDonDetailDTO();
    dto.setHoaDon(hoaDonDTO);
    dto.setThanhToans(thanhToanDTOs);

    return dto;
}
}
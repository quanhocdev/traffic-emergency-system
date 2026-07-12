package com.example.suco.service.tienich.tien.user;

import com.example.suco.dto.tienich.tien.quanly.ThongKeQuyRequestDTO;
import com.example.suco.dto.tienich.tien.quanly.ThongKeQuyResponseDTO;
import com.example.suco.dto.tienich.tien.quanly.VinhDanhDTO;
import com.example.suco.repository.tienich.tien.ThongKeQuyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ThongKeQuyService {

    @Autowired
    private ThongKeQuyRepository vinhDanhRepository;

    public ThongKeQuyResponseDTO getThongKe(
        ThongKeQuyRequestDTO dto
) {

    Long tong;
    List<VinhDanhDTO> bangVinhDanh;

    if (dto.getTuNgay() == null || dto.getDenNgay() == null) {

        tong = vinhDanhRepository.sumTongQuyenGop();

        bangVinhDanh = vinhDanhRepository.findBangVinhDanh();

    } else {

        LocalDateTime start =
                dto.getTuNgay().atStartOfDay();

        LocalDateTime end =
                dto.getDenNgay()
                        .atTime(23, 59, 59);
        tong =
                vinhDanhRepository.sumQuyenGopTheoThoiGian(
                        start,
                        end
                );

        bangVinhDanh =
                vinhDanhRepository.findBangVinhDanhTheoThoiGian(
                        start,
                        end
                );
    }

    if (tong == null) {
        tong = 0L;
    }

    return new ThongKeQuyResponseDTO(
            tong,
            bangVinhDanh
    );
}
}
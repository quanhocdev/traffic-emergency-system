package com.example.suco.mapper;

import com.example.suco.dto.suco.baocao.SuCoResponseDTO;
import com.example.suco.dto.suco.baocao.TheoDoiBaoCaoResponseDTO;
import com.example.suco.model.BaoCaoSuCo;
import org.springframework.stereotype.Component;

@Component
public class SuCoMapper {

    public SuCoResponseDTO toDto(BaoCaoSuCo b) {
        String tenLoai =
                (b.getLoaiSuCo() != null)
                        ? b.getLoaiSuCo().getTen()
                        : "Không xác định";

        String iconUrl =
                (b.getLoaiSuCo() != null)
                        ? b.getLoaiSuCo().getIconUrl()
                        : "";

        String tenNguoiBao =
                (b.getReporter() != null)
                        ? b.getReporter().getName()
                        : "Người dân báo";

        return new SuCoResponseDTO(
                b.getId(),
                b.getViDo(),
                b.getKinhDo(),
                b.getMoTa(),
                tenLoai,
                b.getTrangThaiDuyet(),
                b.getTrangThaiXuLy(),
                iconUrl,
                b.getMucDoNghiemTrong(),
                b.getHinhAnhUrl(),
                b.getDoTinCay(),
                null,
                null,
                null,
                null,
                b.getDiaChi(),
                tenNguoiBao
        );
    }
    public TheoDoiBaoCaoResponseDTO toTheoDoiDto(BaoCaoSuCo b) {
    TheoDoiBaoCaoResponseDTO dto =
            new TheoDoiBaoCaoResponseDTO();

    dto.setId(b.getId());

    dto.setTenLoai(
            b.getLoaiSuCo() != null
                    ? b.getLoaiSuCo().getTen()
                    : null
    );

    dto.setMoTa(b.getMoTa());
    dto.setHinhAnhUrl(b.getHinhAnhUrl());
    dto.setDiaChi(b.getDiaChi());

    dto.setTrangThaiDuyet(b.getTrangThaiDuyet());
    dto.setTrangThaiXuLy(b.getTrangThaiXuLy());

    dto.setDoTinCay(b.getDoTinCay());
    dto.setMucDoNghiemTrong(b.getMucDoNghiemTrong());

    dto.setThoiGianTao(b.getThoiGianTao());

    if (b.getTruSoTiepNhan() != null) {
        dto.setIdTruSoTiepNhan(
                b.getTruSoTiepNhan().getId()
        );

        dto.setTenTruSoTiepNhan(
                b.getTruSoTiepNhan().getTenTruSo()
        );
    }

    return dto;
}
}
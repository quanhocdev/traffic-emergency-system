package com.example.suco.mapper;

import com.example.suco.dto.suco.baocao.SuCoResponseDTO;
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
}
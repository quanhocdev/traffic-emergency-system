package com.example.suco.mapper;
import com.example.suco.model.LoaiSuCo;
import com.example.suco.dto.suco.baocao.SuCoMapResponseDTO;
import com.example.suco.dto.suco.baocao.SuCoRequestDTO;
import com.example.suco.dto.suco.baocao.TheoDoiSuCoDetailResponseDTO;
import com.example.suco.dto.sos.hoadon.quanly.TruSoMiniDTO;
import com.example.suco.dto.suco.baocao.AdminSuCoDetailResponseDTO;
import com.example.suco.dto.suco.baocao.TruSoSuCoDetailResponseDTO;
import com.example.suco.dto.suco.baocao.UserSuCoDetailResponseDTO;
import com.example.suco.model.BaoCaoSuCo;
import org.springframework.stereotype.Component;

@Component
public class SuCoMapper {



        public BaoCaoSuCo toEntity(
            SuCoRequestDTO dto){
                BaoCaoSuCo entity = new BaoCaoSuCo();
                entity.setViDo(dto.getViDo());
                entity.setKinhDo(dto.getKinhDo());
                entity.setMoTa(dto.getMoTa());

                LoaiSuCo loaiSuCo = new LoaiSuCo();
                loaiSuCo.setId(dto.getLoaiSuCoId());
                entity.setLoaiSuCo(loaiSuCo);

                entity.setHinhAnhUrl(dto.getHinhAnhUrl());

                return entity;
            }
        
        public SuCoMapResponseDTO toMapDto(BaoCaoSuCo b) {
                SuCoMapResponseDTO dto = new SuCoMapResponseDTO();
                dto.setId(b.getId());
                dto.setViDo(b.getViDo());
                dto.setKinhDo(b.getKinhDo());
                dto.setIconUrl( b.getLoaiSuCo() != null ? b.getLoaiSuCo().getIconUrl() : null);
                dto.setTrangThaiXuLy(b.getTrangThaiXuLy() != null ? b.getTrangThaiXuLy().getLabel() : null);
                dto.setMucDoSuCo(
    b.getMucDoSuCo() != null ? b.getMucDoSuCo().name() : null
);
                dto.setTruSoId(b.getTruSoTiepNhan() != null ? b.getTruSoTiepNhan().getId() : null);
                return dto;
        }

    public AdminSuCoDetailResponseDTO toAdminDetailDto(BaoCaoSuCo b) {

    AdminSuCoDetailResponseDTO dto =
            new AdminSuCoDetailResponseDTO();

            
    dto.setId(b.getId());
    dto.setViDo(b.getViDo());
    dto.setKinhDo(b.getKinhDo());
    dto.setReporterUid(
        b.getReporter() != null
                ? b.getReporter().getUid()
                : null
);
    dto.setMoTa(b.getMoTa());

    dto.setTenLoai(
            b.getLoaiSuCo() != null
                    ? b.getLoaiSuCo().getTen()
                    : null
    );

    dto.setIconUrl(
            b.getLoaiSuCo() != null
                    ? b.getLoaiSuCo().getIconUrl()
                    : null
    );
    dto.setTenNguoiBao(
            b.getReporter() != null
                    ? b.getReporter().getName()
                    : "Người dân báo"
    );


if (b.getTruSoTiepNhan() != null) {
    TruSoMiniDTO truSoTiepNhan = new TruSoMiniDTO();
    truSoTiepNhan.setId(b.getTruSoTiepNhan().getId());
    truSoTiepNhan.setTenTruSo(b.getTruSoTiepNhan().getTenTruSo());

    dto.setTruSoTiepNhan(truSoTiepNhan);
}

    dto.setTrangThaiDuyet(b.getTrangThaiDuyet() != null ? b.getTrangThaiDuyet().getLabel() : null);
    dto.setTrangThaiXuLy(b.getTrangThaiXuLy() != null ? b.getTrangThaiXuLy().getLabel() : null);

    dto.setMucDoNghiemTrong(b.getMucDoSuCo() != null ? b.getMucDoSuCo().getLabel() : null);
    dto.setHinhAnhUrl(b.getHinhAnhUrl());

    dto.setDoTinCay(b.getDoTinCay());

    dto.setTenNguoiBao(
            b.getReporter() != null
                    ? b.getReporter().getName()
                    : "Người dân báo"
    );

    dto.setDiaChi(b.getDiaChi());
    dto.setThoiGianTao(b.getThoiGianTao());

    return dto;
}
public TruSoSuCoDetailResponseDTO toTruSoDetailDto(BaoCaoSuCo b) {

    TruSoSuCoDetailResponseDTO dto =
            new TruSoSuCoDetailResponseDTO();

            
    dto.setId(b.getId());
    dto.setViDo(b.getViDo());
    dto.setKinhDo(b.getKinhDo());
    dto.setMoTa(b.getMoTa());

    dto.setTenLoai(
            b.getLoaiSuCo() != null
                    ? b.getLoaiSuCo().getTen()
                    : null
    );

    dto.setIconUrl(
            b.getLoaiSuCo() != null
                    ? b.getLoaiSuCo().getIconUrl()
                    : null
    );
    dto.setTenNguoiBao(
            b.getReporter() != null
                    ? b.getReporter().getName()
                    : "Người dân báo"
    );

if (b.getTruSoTiepNhan() != null) {
    TruSoMiniDTO truSoTiepNhan = new TruSoMiniDTO();
    truSoTiepNhan.setId(b.getTruSoTiepNhan().getId());
    truSoTiepNhan.setTenTruSo(b.getTruSoTiepNhan().getTenTruSo());

    dto.setTruSoTiepNhan(truSoTiepNhan);
}

    dto.setTrangThaiDuyet(b.getTrangThaiDuyet() != null ? b.getTrangThaiDuyet().getLabel() : null);
    dto.setTrangThaiXuLy(b.getTrangThaiXuLy() != null ? b.getTrangThaiXuLy().getLabel() : null);

    dto.setMucDoSuCo(
    b.getMucDoSuCo() != null ? b.getMucDoSuCo().name() : null
);
    dto.setHinhAnhUrl(b.getHinhAnhUrl());

    dto.setDoTinCay(b.getDoTinCay());

    dto.setTenNguoiBao(
            b.getReporter() != null
                    ? b.getReporter().getName()
                    : "Người dân báo"
    );

    dto.setDiaChi(b.getDiaChi());
    dto.setThoiGianTao(b.getThoiGianTao());


    
    return dto;
}

    public UserSuCoDetailResponseDTO toUserDetailDto(BaoCaoSuCo b) {

    UserSuCoDetailResponseDTO dto =
            new UserSuCoDetailResponseDTO();

    dto.setId(b.getId());
    dto.setViDo(b.getViDo());
    dto.setKinhDo(b.getKinhDo());
    dto.setMoTa(b.getMoTa());

    dto.setTenLoai(
            b.getLoaiSuCo() != null
                    ? b.getLoaiSuCo().getTen()
                    : null
    );

    dto.setIconUrl(
            b.getLoaiSuCo() != null
                    ? b.getLoaiSuCo().getIconUrl()
                    : null
    );

    dto.setTrangThaiDuyet(b.getTrangThaiDuyet() != null ? b.getTrangThaiDuyet().getLabel() : null);
    dto.setTrangThaiXuLy(b.getTrangThaiXuLy() != null ? b.getTrangThaiXuLy().getLabel() : null);

    dto.setMucDoNghiemTrong(b.getMucDoSuCo() != null ? b.getMucDoSuCo().getLabel() : null);
    dto.setHinhAnhUrl(b.getHinhAnhUrl());

    dto.setDoTinCay(b.getDoTinCay());

    dto.setTenNguoiBao(
            b.getReporter() != null
                    ? b.getReporter().getName()
                    : "Người dân báo"
    );

    dto.setDiaChi(b.getDiaChi());
    dto.setThoiGianTao(b.getThoiGianTao());

    return dto;
}

    public TheoDoiSuCoDetailResponseDTO toTheoDoiDto(BaoCaoSuCo b) {
    TheoDoiSuCoDetailResponseDTO dto =
            new TheoDoiSuCoDetailResponseDTO();

    dto.setId(b.getId());

    dto.setTenLoai(
            b.getLoaiSuCo() != null
                    ? b.getLoaiSuCo().getTen()
                    : null
    );

    dto.setMoTa(b.getMoTa());
    dto.setHinhAnhUrl(b.getHinhAnhUrl());
    dto.setDiaChi(b.getDiaChi());

    dto.setTrangThaiDuyet(
        b.getTrangThaiDuyet() != null
                ? b.getTrangThaiDuyet().getLabel()
                : null
);
    dto.setTrangThaiXuLy(b.getTrangThaiXuLy() != null
            ? b.getTrangThaiXuLy().getLabel()
            : null
    );

    dto.setDoTinCay(b.getDoTinCay());
    dto.setMucDoNghiemTrong(
        b.getMucDoSuCo() != null
                ? b.getMucDoSuCo().getLabel()
                : null
        );

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
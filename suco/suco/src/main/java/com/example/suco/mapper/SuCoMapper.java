package com.example.suco.mapper;
import com.example.suco.model.LoaiSuCo;
import com.example.suco.dto.info.truso.TruSoMapDto;
import com.example.suco.dto.info.truso.TruSoMiniDTO;
import com.example.suco.dto.info.user.UserInfoResponseDTO;
import com.example.suco.dto.suco.baocao.SuCoMapResponseDTO;
import com.example.suco.dto.suco.baocao.admin.AdminSuCoDetailResponseDTO;
import com.example.suco.dto.suco.baocao.truso.TruSoSuCoDetailResponseDTO;
import com.example.suco.dto.suco.baocao.user.SuCoRequestDTO;
import com.example.suco.dto.suco.baocao.user.TheoDoiSuCoDetailResponseDTO;
import com.example.suco.dto.suco.baocao.user.TheoDoiSuCoItemResponseDTO;
import com.example.suco.dto.suco.baocao.user.UserSuCoDetailResponseDTO;
import com.example.suco.mapper.info.InfoTruSoMapper;
import com.example.suco.mapper.info.InfoUserMapper;
import com.example.suco.model.BaoCaoSuCo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SuCoMapper {

        @Autowired
        private InfoTruSoMapper infoTruSoMapper;

        @Autowired
        private InfoUserMapper infoUserMapper;

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

        AdminSuCoDetailResponseDTO dto = new AdminSuCoDetailResponseDTO();

        dto.setId(b.getId());
        dto.setViDo(b.getViDo());
        dto.setKinhDo(b.getKinhDo());
        dto.setReporter(infoUserMapper.toUserMiniDTO(b.getReporter()));
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
        dto.setReporter(infoUserMapper.toUserMiniDTO(b.getReporter()));

        dto.setTruSoTiepNhan(infoTruSoMapper.toMiniDto(b.getTruSoTiepNhan()));


        dto.setTrangThaiXuLy(b.getTrangThaiXuLy() != null ? b.getTrangThaiXuLy().getLabel() : null);

        dto.setMucDoSuCo(
                b.getMucDoSuCo() != null ? b.getMucDoSuCo().name() : null
        );
        dto.setHinhAnhUrl(b.getHinhAnhUrl());

        dto.setDoTinCay(b.getDoTinCay());

        dto.setReporter(infoUserMapper.toUserMiniDTO(b.getReporter()));

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

        dto.setTruSoTiepNhan(infoTruSoMapper.toMiniDto(b.getTruSoTiepNhan()));

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

public TheoDoiSuCoItemResponseDTO toTheoDoiItemDto(BaoCaoSuCo b, String tenTruSo) {
        if (b == null) return null;
        
        TheoDoiSuCoItemResponseDTO dto = new TheoDoiSuCoItemResponseDTO();
        
        dto.setId(b.getId());
        dto.setTenLoai(b.getLoaiSuCo() != null ? b.getLoaiSuCo().getTen() : null);
        dto.setHinhAnhUrl(b.getHinhAnhUrl());
        dto.setTrangThaiXuLy(b.getTrangThaiXuLy() != null ? b.getTrangThaiXuLy().getLabel() : null);
        dto.setThoiGianTao(b.getThoiGianTao());
        dto.setTenTruSoTiepNhan(tenTruSo);

        return dto;
    }

    public TheoDoiSuCoDetailResponseDTO toTheoDoiDto(BaoCaoSuCo b, TruSoMapDto truSoDto, UserInfoResponseDTO user) {
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

dto.setTruSo(truSoDto);
dto.setUser(user);


    return dto;
}
}
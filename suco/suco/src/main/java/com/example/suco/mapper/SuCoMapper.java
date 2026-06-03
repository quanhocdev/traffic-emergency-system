package com.example.suco.mapper;
import com.example.suco.model.User;
import com.example.suco.model.LoaiSuCo;
import com.example.suco.model.TruSo;
import com.example.suco.dto.suco.baocao.SuCoMapResponseDTO;
import com.example.suco.dto.suco.baocao.SuCoRequestDTO;
import com.example.suco.dto.suco.baocao.TheoDoiBaoCaoResponseDTO;
import com.example.suco.dto.sos.hoadon.quanly.TruSoMiniDTO;
import com.example.suco.dto.suco.baocao.AdminSuCoDetailResponseDTO;
import com.example.suco.dto.suco.baocao.UserSuCoDetailResponseDTO;
import com.example.suco.dto.vanhanh.truso.TruSoMapDto;
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
                dto.setTrangThaiXuLy(b.getTrangThaiXuLy());
                dto.setMucDoNghiemTrong(b.getMucDoNghiemTrong());
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

    if (b.getTruSoDeXuat() != null) {
    TruSoMiniDTO truSoDeXuat = new TruSoMiniDTO();
    truSoDeXuat.setId(b.getTruSoDeXuat().getId());
    truSoDeXuat.setTenTruSo(b.getTruSoDeXuat().getTenTruSo());

    dto.setTruSoDeXuat(truSoDeXuat);
}

if (b.getTruSoTiepNhan() != null) {
    TruSoMiniDTO truSoTiepNhan = new TruSoMiniDTO();
    truSoTiepNhan.setId(b.getTruSoTiepNhan().getId());
    truSoTiepNhan.setTenTruSo(b.getTruSoTiepNhan().getTenTruSo());

    dto.setTruSoTiepNhan(truSoTiepNhan);
}

    dto.setTrangThaiDuyet(b.getTrangThaiDuyet());
    dto.setTrangThaiXuLy(b.getTrangThaiXuLy());

    dto.setMucDoNghiemTrong(b.getMucDoNghiemTrong());
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

    dto.setTrangThaiDuyet(b.getTrangThaiDuyet());
    dto.setTrangThaiXuLy(b.getTrangThaiXuLy());

    dto.setMucDoNghiemTrong(b.getMucDoNghiemTrong());
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
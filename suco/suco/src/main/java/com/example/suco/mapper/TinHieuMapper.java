package com.example.suco.mapper;

import com.example.suco.dto.sos.hoadon.quanly.TruSoMiniDTO;
import com.example.suco.dto.sos.tinhieu.SOSMapResponseDTO;
import com.example.suco.dto.sos.tinhieu.UserMiniDTO;
import com.example.suco.dto.sos.tinhieu.admin.AdminSOSDetailResponseDTO;
import com.example.suco.dto.sos.tinhieu.truso.TruSoSOSDetailResponseDTO;
import com.example.suco.dto.sos.tinhieu.user.TheoDoiSOSDetailResponseDTO;
import com.example.suco.dto.sos.tinhieu.user.TheoDoiSOSItemResponseDTO;
import com.example.suco.dto.sos.tinhieu.user.TinHieuSOSRequestDTO;
import com.example.suco.model.User;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.dto.vanhanh.truso.TruSoMapDto;
import com.example.suco.dto.sos.tinhieu.UserInfoResponseDTO;
import com.example.suco.model.enums.TrangThaiXuLy; // 🔥 Import Enum vào đây
import org.springframework.stereotype.Component;
import com.example.suco.dto.sos.hoadon.quanly.HoaDonResponseDTO;

@Component
public class TinHieuMapper {

    private UserMiniDTO toUserMiniDTO(User user) {
        if (user == null) {
            return null;
        }

        UserMiniDTO dto = new UserMiniDTO();
        dto.setId(user.getUid());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setTotalPoints(user.getTotalPoints());

        return dto;
    }

    // Request DTO → Entity
    public TinHieuSOS toEntity(
            TinHieuSOSRequestDTO dto,
            String uid,
            User user
    ) {
        TinHieuSOS sos = new TinHieuSOS();

        // user info
        sos.setUserId(uid);
        sos.setUser(user);

        // business data
        sos.setViDo(dto.getViDo());
        sos.setKinhDo(dto.getKinhDo());
        sos.setGhiChu(dto.getGhiChu());

        sos.setTrangThai(TrangThaiXuLy.DA_TIEP_NHAN);

        return sos;
    }

    public SOSMapResponseDTO toMapDto(TinHieuSOS sos) {
        SOSMapResponseDTO dto = new SOSMapResponseDTO();

        dto.setId(sos.getId());
        dto.setViDo(sos.getViDo());
        dto.setKinhDo(sos.getKinhDo());
        
        // 🔥 Đổi sang .name() để truyền chuỗi String ra DTO (giữ nguyên kiểu dữ liệu của DTO)
        dto.setTrangThai(sos.getTrangThai() != null ? sos.getTrangThai().name() : null);

        TruSoMiniDTO mini = new TruSoMiniDTO();
        mini.setId(sos.getIdTruSoTiepNhan()); 
        mini.setTenTruSo(null);

        dto.setTruSo(mini);

        return dto;
    }

    // Entity → ResponseDTO TruSo
    public TruSoSOSDetailResponseDTO toTruSoDetailDto(TinHieuSOS sos) {
        if (sos == null) {
            return null;
        }

        TruSoSOSDetailResponseDTO dto = new TruSoSOSDetailResponseDTO();

        dto.setId(sos.getId());
        dto.setViDo(sos.getViDo());
        dto.setKinhDo(sos.getKinhDo());
        dto.setDiaChi(sos.getDiaChi());
        dto.setGhiChu(sos.getGhiChu());
        dto.setHinhAnhUrl(sos.getHinhAnh());
        dto.setGhiAmUrl(sos.getGhiAm());
        
        // 🔥 Chuyển đổi Enum thành String .name()
        dto.setTrangThai(sos.getTrangThai() != null ? sos.getTrangThai().name() : null);
        dto.setThoiGianTao(sos.getCreatedAt());

        // Map thông tin người gửi cứu hộ
        dto.setNguoiGui(toUserMiniDTO(sos.getUser()));

        // 🔥 GẮN CHUẨN ĐỐI TƯỢNG HÓA ĐƠN TỪ PACKAGE QUANLY
        if (sos.getHoaDon() != null) {
            HoaDonResponseDTO hdDto = new HoaDonResponseDTO();
            
            hdDto.setId(sos.getHoaDon().getId());
            hdDto.setSosId(sos.getHoaDon().getSosId());
            hdDto.setTrusoId(sos.getHoaDon().getTrusoId());
            hdDto.setUserId(sos.getHoaDon().getUserId());
            hdDto.setNoiDungXuLy(sos.getHoaDon().getNoiDungXuLy());
            hdDto.setThanhTien(sos.getHoaDon().getThanhTien());
            hdDto.setCreatedAt(sos.getHoaDon().getCreatedAt());
            hdDto.setTrangThai(sos.getHoaDon().getTrangThai());
            
            dto.setHoaDon(hdDto);
        }

        return dto;
    }

    // Entity → ResponseDTO Admin
    public AdminSOSDetailResponseDTO toAdminDetailDto(
            TinHieuSOS sos,
            TruSoMiniDTO truSoDto
    ) {
        AdminSOSDetailResponseDTO dto = new AdminSOSDetailResponseDTO();

        dto.setId(sos.getId());
        dto.setViDo(sos.getViDo());
        dto.setKinhDo(sos.getKinhDo());
        dto.setDiaChi(sos.getDiaChi());
        dto.setGhiChu(sos.getGhiChu());
        dto.setHinhAnhUrl(sos.getHinhAnh());
        dto.setGhiAmUrl(sos.getGhiAm());
        dto.setThoiGianTao(sos.getCreatedAt());
        
        // 🔥 Chuyển đổi Enum thành String .name()
        dto.setTrangThai(sos.getTrangThai() != null ? sos.getTrangThai().name() : null);
        dto.setUserId(sos.getUserId());
        dto.setNguoiGui(toUserMiniDTO(sos.getUser()));

        dto.setTruSoTiepNhan(truSoDto);
        return dto;
    }

    // Entity → ItemResponseDTO cá nhân
    public TheoDoiSOSItemResponseDTO toTheoDoiItemDto(
            TinHieuSOS sos, String tenTruSo
    ) {
        TheoDoiSOSItemResponseDTO dto = new TheoDoiSOSItemResponseDTO();

        dto.setId(sos.getId());
        dto.setHinhAnh(sos.getHinhAnh());
        
        // 🔥 Chuyển đổi Enum thành String .name()
        dto.setTrangThai(sos.getTrangThai() != null ? sos.getTrangThai().name() : null);
        dto.setCreatedAt(sos.getCreatedAt());
        dto.setTenTruSo(tenTruSo);

        return dto;
    }

    // Entity → ResponseDTO cá nhân
    public TheoDoiSOSDetailResponseDTO toTheoDoiDto(TinHieuSOS sos, TruSoMapDto truSoDto, UserInfoResponseDTO user) {
        TheoDoiSOSDetailResponseDTO dto = new TheoDoiSOSDetailResponseDTO();

        dto.setId(sos.getId());
        dto.setViDo(sos.getViDo());
        dto.setKinhDo(sos.getKinhDo());
        dto.setDiaChi(sos.getDiaChi());
        dto.setGhiChu(sos.getGhiChu());
        dto.setHinhAnh(sos.getHinhAnh());
        dto.setGhiAm(sos.getGhiAm());

        // 🔥 Chuyển đổi Enum thành String .name()
        dto.setTrangThai(sos.getTrangThai() != null ? sos.getTrangThai().name() : null);
        dto.setCreatedAt(sos.getCreatedAt());

        if (sos.getHoaDon() != null) {
            dto.setHoaDonId(sos.getHoaDon().getId());
            dto.setThanhTien(sos.getHoaDon().getThanhTien());
            dto.setTrangThaiHoaDon(sos.getHoaDon().getTrangThai());
        }
        
        dto.setTruSo(truSoDto);
        dto.setUser(user);

        return dto;
    }
}
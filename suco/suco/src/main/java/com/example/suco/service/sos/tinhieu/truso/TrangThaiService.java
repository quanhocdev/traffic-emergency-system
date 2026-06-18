package com.example.suco.service.sos.tinhieu.truso;

import com.example.suco.mapper.TinHieuMapper;
import com.example.suco.dto.sos.tinhieu.SOSMapResponseDTO;
import com.example.suco.dto.sos.tinhieu.truso.TruSoSOSDetailResponseDTO;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.TruSo;
import com.example.suco.model.enums.TrangThaiXuLy; 
import com.example.suco.repository.sos.tinhieu.TinHieuSOSRepository;
import com.example.suco.service.sos.tinhieu.notification.TinHieuRealtimeService;
import com.example.suco.service.sos.tinhieu.truso.validation.CheckTrangThaiService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TrangThaiService {

    @Autowired
    private TinHieuSOSRepository tinHieuSOSRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private TinHieuMapper tinHieuMapper;

    @Autowired
    private TinHieuRealtimeService tinHieuRealtimeService;

    @Autowired
    private CheckTrangThaiService checkTrangThaiService;

    public void capNhatTrangThaiSOS(
            Long id,
            String statusStr, // Đổi tên biến thành statusStr cho rõ bản chất là chuỗi nhận vào
            TruSo current
    ) {
        if (statusStr != null) {
            statusStr = statusStr.split(",")[0].trim();
        }

        TinHieuSOS sos = tinHieuSOSRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy SOS"
                ));

        String currentStatusStr = sos.getTrangThai() != null ? sos.getTrangThai().name() : null;

        // Giữ nguyên kiểm tra validation cũ từ Service kiểm tra của bạn
        checkTrangThaiService.validateAll(
                sos,
                currentStatusStr,
                statusStr,
                current
        );

        TrangThaiXuLy targetStatus;
        try {
            // Trường hợp Front-end truyền chuỗi đặc biệt cũ, map sang Enum tương ứng
            if ("DANG_CUU_TRO".equalsIgnoreCase(statusStr)) {
                targetStatus = TrangThaiXuLy.DANG_DI_CHUYEN;
            } else if ("DA_HUY".equalsIgnoreCase(statusStr)) {
                targetStatus = TrangThaiXuLy.HUY_BO;
            } else {
                targetStatus = TrangThaiXuLy.valueOf(statusStr.toUpperCase());
            }
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái cập nhật không hợp lệ: " + statusStr);
        }

         if (targetStatus == TrangThaiXuLy.DA_TIEP_NHAN || 
            targetStatus == TrangThaiXuLy.DANG_DI_CHUYEN || 
            targetStatus == TrangThaiXuLy.DANG_XU_LY) {
            sos.setIdTruSoTiepNhan(current.getId());
        }

        sos.setTrangThai(targetStatus);
        tinHieuSOSRepository.save(sos);

        notify(sos, current);
    }

    private void notify(TinHieuSOS sos, TruSo current) {
        tinHieuRealtimeService.guiThongDiep(sos);

        SOSMapResponseDTO dto = tinHieuMapper.toMapDto(sos);

        Long targetTruSo =
                sos.getIdTruSoTiepNhan() != null
                        ? sos.getIdTruSoTiepNhan()
                        : current.getId();

        messagingTemplate.convertAndSend(
                "/topic/truso/" + targetTruSo,
                dto
        );

        messagingTemplate.convertAndSend(
                "/topic/admin",
                dto
        );
    }

    public List<TruSoSOSDetailResponseDTO> layDanhSachSOSActive(
            TruSo current,
            String status
    ) {
        if (current == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Chưa đăng nhập"
            );
        }

        return tinHieuSOSRepository.findActiveSOSByTruSo(current.getId())
                .stream()
                .filter(sos -> {
                        if (status == null || status.isEmpty()) return true;
                        String currentEnumStr = sos.getTrangThai() != null ? sos.getTrangThai().name() : "";
                        
                        // Hỗ trợ quét cả các alias cũ khi lọc danh sách
                        if ("DANG_CUU_TRO".equalsIgnoreCase(status)) {
                            return TrangThaiXuLy.DANG_DI_CHUYEN.name().equalsIgnoreCase(currentEnumStr);
                        }
                        if ("DA_HUY".equalsIgnoreCase(status)) {
                            return TrangThaiXuLy.HUY_BO.name().equalsIgnoreCase(currentEnumStr);
                        }
                        return status.equalsIgnoreCase(currentEnumStr);
                })
                .map(tinHieuMapper::toTruSoDetailDto)
                .toList();
    }

    public TruSoSOSDetailResponseDTO layChiTietSOSChoTruSo(Long id, TruSo current) {
        if (current == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Vui lòng đăng nhập tài khoản trụ sở!"
            );
        }

        TinHieuSOS sos = tinHieuSOSRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy tín hiệu SOS này!"
                ));

        return tinHieuMapper.toTruSoDetailDto(sos);
    }
    public List<TruSoSOSDetailResponseDTO> layLichSuSOSChoTruSo(Long truSoId) {

    List<TinHieuSOS> danhSachEntity = tinHieuSOSRepository.findHistoryByTruSo(truSoId);
    
    // 2. Stream và map thẳng qua hàm toTruSoDetailDto của TinHieuMapper
    return danhSachEntity.stream()
            .map(tinHieuMapper::toTruSoDetailDto)
            .toList();
}
}
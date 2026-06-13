package com.example.suco.service.sos.tinhieu.truso;

import com.example.suco.mapper.TinHieuMapper;
import com.example.suco.dto.sos.tinhieu.SOSMapResponseDTO;
import com.example.suco.dto.sos.tinhieu.truso.TruSoSOSDetailResponseDTO;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.TruSo;
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
            String status,
            TruSo current
    ) {
        if (status != null) {
            status = status.split(",")[0].trim();
        }

        TinHieuSOS sos = tinHieuSOSRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy SOS"
                ));

        String currentStatus = sos.getTrangThai();

        checkTrangThaiService.validateAll(
                sos,
                currentStatus,
                status,
                current
        );

        // =========================================================================
        // ĐÓNG DẤU ID TRỤ SỞ: Đảm bảo khi tiếp nhận/xử lý thì bản ghi được gắn với Trụ sở
        // Tránh tình trạng cột id_tru_so_tiep_nhan trong DB bị NULL gây mất ghim khi F5
        // =========================================================================
        if ("DA_TIEP_NHAN".equals(status) || "DANG_XU_LY".equals(status) || "DANG_CUU_TRO".equals(status)) {
            sos.setIdTruSoTiepNhan(current.getId());
        }

        if ("HOAN_THANH".equals(status)) {
            sos.setTrangThai("HOAN_THANH");
            tinHieuSOSRepository.save(sos);
            notify(sos, current);
            return;
        }

        if ("DA_HUY".equals(status)) {
            sos.setTrangThai("DA_HUY");
            tinHieuSOSRepository.save(sos);
            notify(sos, current);
            return;
        }

        sos.setTrangThai(status);
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

        // Đã đồng bộ hàm quét đa trạng thái dở dang từ Repository
        return tinHieuSOSRepository.findActiveSOSByTruSo(current.getId())
                .stream()
                .filter(sos ->
                        status == null ||
                        status.isEmpty() ||
                        status.equalsIgnoreCase(sos.getTrangThai())
                )
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
}
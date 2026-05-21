package com.example.suco.service.sos.truso;

import com.example.suco.dto.sos.TinHieuSOSResponseDTO;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.TruSo;
import com.example.suco.repository.TinHieuSOSRepository;
import com.example.suco.service.dieuphoi.engine.DispatchEngineService;
import com.example.suco.service.sos.system.mapper.TinHieuMapper;
import com.example.suco.service.sos.system.notification.TinHieuRealtimeService;
import com.example.suco.service.sos.system.validation.CheckTrangThaiService;
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
    private DispatchEngineService dispatchEngineService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private TinHieuMapper tinHieuMapper;

    @Autowired
    private TinHieuRealtimeService tinHieuRealtimeService;

    @Autowired
    private CheckTrangThaiService checkTrangThaiService;

    // =========================================
    // UPDATE TRẠNG THÁI
    // =========================================
    public void capNhatTrangThaiSOS(Long id, String status, TruSo current) {

        if (status != null) {
            status = status.split(",")[0].trim();
        }

        TinHieuSOS sos = tinHieuSOSRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy SOS"
                ));

        String currentStatus = sos.getTrangThai();

        checkTrangThaiService.validateAll(sos, currentStatus, status, current);

        // =================================================
        // 1. TỪ CHỐI
        // =================================================
        if ("TU_CHOI".equals(status)) {

dispatchEngineService.reject(sos);

            notify(sos, current);
            return;
        }

        // =================================================
        // 2. TIMEOUT (nếu FE hoặc scheduler gọi)
        // =================================================
        if ("TIMEOUT".equals(status)) {
dispatchEngineService.timeout(sos);

            notify(sos, current);
            return;
        }

        // =================================================
        // 3. TIẾP NHẬN
        // =================================================
        if ("TIEP_NHAN".equals(status)) {

            sos.setTrangThai("DANG_XU_LY");
            sos.setIdTruSoTiepNhan(current.getId());

            dispatchEngineService.accept(sos, current.getId());

            tinHieuSOSRepository.save(sos);
            notify(sos, current);
            return;
        }

        // =================================================
        // 4. HỦY
        // =================================================
        if ("HUY_BO".equals(status)) {

            sos.setTrangThai("HUY_BO");

            dispatchEngineService.cancel(sos);

            tinHieuSOSRepository.save(sos);

            notify(sos, current);
            return;
        }

        // =================================================
        // 5. DEFAULT UPDATE
        // =================================================
        sos.setTrangThai(status);
        tinHieuSOSRepository.save(sos);

        notify(sos, current);
    }

    // =========================================
    // COMMON NOTIFY
    // =========================================
    private void notify(TinHieuSOS sos, TruSo current) {

        tinHieuRealtimeService.guiThongDiep(sos);

        TinHieuSOSResponseDTO dto = tinHieuMapper.mapToDTO(sos);

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

    // =========================================
    // LIST ACTIVE
    // =========================================
    public List<TinHieuSOSResponseDTO> layDanhSachSOSActive(
            TruSo current,
            String status
    ) {

        if (current == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Chưa đăng nhập"
            );
        }

        return tinHieuSOSRepository.findActiveByTruSo(current.getId())
                .stream()
                .filter(sos ->
                        status == null ||
                        status.isEmpty() ||
                        status.equalsIgnoreCase(sos.getTrangThai())
                )
                .map(tinHieuMapper::mapToDTO)
                .toList();
    }
}
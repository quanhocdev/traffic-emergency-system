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

        // 2. HANDLE TU_CHOI
        if ("TU_CHOI".equals(status)) {

            dispatchEngineService.moveNext(sos);

            TinHieuSOSResponseDTO dto = tinHieuMapper.mapToDTO(sos);

            messagingTemplate.convertAndSend(
                    "/topic/truso/" + current.getId(),
                    dto
            );

            return;
        }

        // 3. UPDATE STATUS
        sos.setTrangThai(status);

        if ("DANG_XU_LY".equals(status)) {
            sos.setIdTruSoTiepNhan(current.getId());
        }

        // 4. BUSINESS RULE END
        if ("HOAN_THANH".equals(status) || "HUY_BO".equals(status)) {
            dispatchEngineService.cancel(sos.getId());
        }

        // 5. SAVE
        tinHieuSOSRepository.save(sos);

        // 6. REALTIME
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
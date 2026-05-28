package com.example.suco.service.sos.tinhieu.quanly;

import com.example.suco.dto.sos.tinhieu.TinHieuSOSResponseDTO;
import com.example.suco.mapper.TinHieuMapper;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.TruSo;
import com.example.suco.repository.sos.tinhieu.TinHieuSOSRepository;
import com.example.suco.service.sos.tinhieu.notification.TinHieuRealtimeService;
import com.example.suco.service.sos.tinhieu.quanly.validation.CheckTrangThaiService;

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

    // =========================================
    // UPDATE TRẠNG THÁI
    // =========================================
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

    // =========================================
    // HOÀN THÀNH
    // =========================================
    if ("HOAN_THANH".equals(status)) {

        sos.setTrangThai("HOAN_THANH");


        tinHieuSOSRepository.save(sos);

        notify(sos, current);

        return;
    }

    // =========================================
    // HỦY
    // =========================================
    if ("DA_HUY".equals(status)) {

        sos.setTrangThai("DA_HUY");

        tinHieuSOSRepository.save(sos);

        notify(sos, current);

        return;
    }

    // =========================================
    // DEFAULT
    // =========================================
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
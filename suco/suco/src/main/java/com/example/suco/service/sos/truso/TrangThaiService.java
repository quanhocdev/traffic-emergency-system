package com.example.suco.service.sos.truso;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.Optional;
import com.example.suco.repository.TinHieuSOSRepository;
import com.example.suco.dto.sos.TinHieuSOSResponseDTO;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.TruSo;
import com.example.suco.service.dieuphoi.engine.DispatchEngineService;
import com.example.suco.service.sos.system.mapper.TinHieuMapper;
import com.example.suco.service.sos.system.notification.TinHieuRealtimeService;
import com.example.suco.service.sos.system.validation.StatusService;
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
    private StatusService statusService;

    @Autowired
    private TinHieuMapper tinHieuMapper;

    @Autowired
private TinHieuRealtimeService tinHieuRealtimeService;

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

        if (!statusService.isValidTransition(currentStatus, status)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Chuyển trạng thái không hợp lệ"
            );
        }

        if ("HOAN_THANH".equals(currentStatus) || "HUY_BO".equals(currentStatus)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "SOS đã kết thúc"
            );
        }

       if ("DANG_XU_LY".equals(status)) {

    if (sos.getIdTruSoDeXuat() == null ||
        !sos.getIdTruSoDeXuat().equals(current.getId())) {

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "SOS không thuộc về trụ sở của bạn"
        );
    }

    sos.setIdTruSoTiepNhan(current.getId());
}
        if ("HOAN_THANH".equals(status)) {

            if (sos.getIdTruSoTiepNhan() == null
                    || !sos.getIdTruSoTiepNhan().equals(current.getId())) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Chỉ trụ sở tiếp nhận mới được hoàn thành"
                );
            }
        }

        if ("TU_CHOI".equals(status)) {

            dispatchEngineService.moveNext(sos);

            TinHieuSOSResponseDTO dto = tinHieuMapper.mapToDTO(sos);

            messagingTemplate.convertAndSend(
                    "/topic/truso/" + current.getId(),
                    dto
            );

            return;
        }

        // update status
        sos.setTrangThai(status);

        // hủy điều phối
        if ("HOAN_THANH".equals(status) || "HUY_BO".equals(status)) {
            dispatchEngineService.cancel(sos.getId());
        }

        tinHieuSOSRepository.save(sos);
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

    List<TinHieuSOS> rawList =
            tinHieuSOSRepository.findActiveByTruSo(current.getId());

    // filter status
    if (status != null && !status.isEmpty()) {

        rawList = rawList.stream()
                .filter(sos ->
                        status.equalsIgnoreCase(sos.getTrangThai())
                )
                .toList();
    }

    // map DTO
    return rawList.stream()
            .map(tinHieuMapper::mapToDTO)
            .toList();
}
}

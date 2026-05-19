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
import com.example.suco.service.DieuPhoiSOSService;
import com.example.suco.service.DieuPhoiSOSService.ThongTinDieuPhoi;
import com.example.suco.service.sos.system.mapper.TinHieuMapper;
import com.example.suco.service.sos.system.validation.StatusService;
@Service
public class TrangThaiService {

    @Autowired
    private TinHieuSOSRepository tinHieuSOSRepository;

    @Autowired
    private DieuPhoiSOSService dieuPhoiService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private StatusService statusService;

    @Autowired
    private TinHieuMapper tinHieuMapper;

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

            Optional<ThongTinDieuPhoi> dpOpt = dieuPhoiService.layThongTinDieuPhoi(id);

            if (dpOpt.isEmpty()
                    || !dpOpt.get().getDanhSachIdTruSo().contains(current.getId())) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "SOS không thuộc về trụ sở của bạn"
                );
            }

            sos.setIdTruSoTiepNhan(current.getId());

            dieuPhoiService.danhDauDaTiepNhan(id, current.getId());
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

            dieuPhoiService.chuyenTiepSangTruSoTiepTheo(id, current.getId());

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
            dieuPhoiService.huyDieuPhoi(id);
        }

        tinHieuSOSRepository.save(sos);

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
}

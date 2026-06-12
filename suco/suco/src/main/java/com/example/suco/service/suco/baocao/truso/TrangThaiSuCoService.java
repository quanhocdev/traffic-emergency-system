package com.example.suco.service.suco.baocao.truso;

import com.example.suco.dto.suco.baocao.SuCoMapResponseDTO;
import com.example.suco.mapper.SuCoMapper;
import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.model.TruSo;
import com.example.suco.model.enums.TrangThaiXuLy;
import com.example.suco.repository.suco.baocao.BaoCaoSuCoRepository;
import com.example.suco.repository.vanhanh.TruSoRepository;
import com.example.suco.service.suco.baocao.system.notification.BaoCaoRealtimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TrangThaiSuCoService {

    @Autowired
    private BaoCaoSuCoRepository reportRepository;

    @Autowired
    private BaoCaoRealtimeService realtimeService;

    @Autowired
private TruSoRepository truSoRepository;

@Autowired
private SuCoMapper suCoMapper;

    private static final Logger log =
        LoggerFactory.getLogger(TrangThaiSuCoService.class);

    @Transactional
public Map<String, Object> updateSuCoStatus(
        Long id,
        TrangThaiXuLy status,
        TruSo current
) {

    BaoCaoSuCo suCo = reportRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy sự cố"
            ));

    log.info("Update status report {} → {}", id, status);


    if (suCo.getTrangThaiXuLy() == TrangThaiXuLy.HOAN_THANH
            || suCo.getTrangThaiXuLy() == TrangThaiXuLy.HUY_BO) {

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Sự cố đã kết thúc, không thể thay đổi trạng thái!"
        );
    }

    if (suCo.getTrangThaiXuLy() == status) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Trạng thái đã là giá trị hiện tại"
        );
    }

    switch (status) {

        case CHO_XU_LY -> {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Không được quay lại CHỜ XỬ LÝ"
            );
        }

        case DANG_XU_LY -> {
            if (suCo.getTrangThaiXuLy() != TrangThaiXuLy.CHO_XU_LY) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Chỉ được tiếp nhận từ CHỜ XỬ LÝ"
                );
            }

            suCo.setTruSoTiepNhan(current);
        }

        case HOAN_THANH -> {
            if (suCo.getTrangThaiXuLy() != TrangThaiXuLy.DANG_XU_LY) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Phải đang xử lý mới được hoàn thành"
                );
            }

            if (suCo.getTruSoTiepNhan() == null ||
                !suCo.getTruSoTiepNhan().getId().equals(current.getId())) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Bạn không có quyền hoàn thành sự cố này!"
                );
            }
        }

        case HUY_BO -> {
            if (suCo.getTrangThaiXuLy() != TrangThaiXuLy.CHO_XU_LY) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Chỉ được hủy khi CHỜ XỬ LÝ"
                );
            }
        }
    }

    suCo.setTrangThaiXuLy(status);

    BaoCaoSuCo saved = reportRepository.save(suCo);

    SuCoMapResponseDTO dto = suCoMapper.toMapDto(saved);

    realtimeService.broadcastReport(dto);

    if (saved.getTruSoTiepNhan() != null) {
        realtimeService.broadcastTruSo(
                saved.getTruSoTiepNhan().getId(),
                dto
        );
    }

    if (saved.getReporter() != null) {
        realtimeService.refreshUserHistory(
                saved.getReporter().getUid()
        );
    }

    return Map.of(
            "message", "Cập nhật trạng thái thành công",
            "newStatus", status
    );
}
    @Transactional
public void updateProcessStatus(
        Long reportId,
        TrangThaiXuLy status,
        Long idTruSoThucTe
) {

    BaoCaoSuCo report = reportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy báo cáo"));

    report.setTrangThaiXuLy(status);

    if (status == TrangThaiXuLy.DANG_XU_LY) {

        TruSo truSo = truSoRepository.findById(idTruSoThucTe)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy trụ sở"
                ));

        report.setTruSoTiepNhan(truSo);
    }

    BaoCaoSuCo saved = reportRepository.save(report);

    SuCoMapResponseDTO dto = suCoMapper.toMapDto(saved);

    realtimeService.broadcastReport(dto);

    if (saved.getTruSoTiepNhan() != null) {
        realtimeService.broadcastTruSo(
                saved.getTruSoTiepNhan().getId(),
                dto
        );
    }
}
}
package com.example.suco.service.suco.baocao.truso;

import com.example.suco.dto.suco.baocao.SuCoMapResponseDTO;
import com.example.suco.mapper.SuCoMapper;
import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.model.TruSo;
import com.example.suco.model.enums.MucDoSuCo;
import com.example.suco.model.enums.TrangThaiXuLy;
import com.example.suco.repository.suco.baocao.BaoCaoSuCoRepository;
import com.example.suco.service.suco.baocao.system.notification.BaoCaoRealtimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@Service
public class MucDoSuCoService {

    private static final Logger log =
            LoggerFactory.getLogger(MucDoSuCoService.class);

    @Autowired
    private BaoCaoSuCoRepository reportRepository;

    @Autowired
    private BaoCaoRealtimeService realtimeService;

@Autowired
private SuCoMapper suCoMapper;
    @Transactional
public Map<String, Object> capNhatMucDo(
        Long id,
        MucDoSuCo mucDo,
        TruSo current
) {


    BaoCaoSuCo suCo = reportRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy sự cố"
            ));


    log.info(
            "\nTrụ sở {} cập nhật mức độ {} sự cố: {}",
            current.getTenTruSo(),
            mucDo,
            id
    );

    if (mucDo == null) {
    throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "Mức độ không hợp lệ"
    );
}

    if (suCo.getTrangThaiXuLy() == TrangThaiXuLy.HOAN_THANH
            || suCo.getTrangThaiXuLy() == TrangThaiXuLy.HUY_BO) {

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Sự cố đã kết thúc, không thể thay đổi mức độ nghiêm trọng!"
        );
    }

    if (suCo.getTrangThaiXuLy() != TrangThaiXuLy.DANG_XU_LY) {

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Chỉ được cập nhật mức độ khi sự cố đang xử lý!"
        );
    }

    if (suCo.getTruSoTiepNhan() == null) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Sự cố chưa được tiếp nhận!"
        );
    }

    if (!current.getId().equals(suCo.getTruSoTiepNhan().getId())) {
        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Bạn không có quyền chỉnh sửa mức độ của sự cố này!"
        );
    }

    suCo.setMucDoSuCo(mucDo);


    BaoCaoSuCo saved = reportRepository.save(suCo);


    SuCoMapResponseDTO dto = suCoMapper.toMapDto(saved);

    
    log.info(
            "\nCập nhật mức độ thành công: {}",
            saved.getMucDoSuCo()
    );

    if (saved.getTruSoTiepNhan() != null) {
        realtimeService.broadcastTruSo(
                saved.getTruSoTiepNhan().getId(),
                dto
        );
    }

    realtimeService.broadcastReport(dto);

    return Map.of(
            "message", "Cập nhật mức độ thành công",
            "mucDoMoi", saved.getMucDoSuCo()
    );
}
}
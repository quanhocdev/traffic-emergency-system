package com.example.suco.service.sos.tinhieu.truso.validation;

import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.TruSo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RulesTrangThaiService {

    public void checkNotNull(TinHieuSOS sos) {
        if (sos == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "SOS không tồn tại");
        }
    }

    public void checkStatusHopLe(String newStatus) {
        if (newStatus == null || newStatus.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái không hợp lệ");
        }
    }

    public void checkDaKetThuc(String currentStatus) {
        if ("HOAN_THANH".equals(currentStatus) ||
            "HUY_BO".equals(currentStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "SOS đã kết thúc");
        }
    }

    public void checkQuyenXuLy(TinHieuSOS sos, TruSo current) {
    if (sos.getIdTruSoTiepNhan() == null ||
        !sos.getIdTruSoTiepNhan().equals(current.getId())) {

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Chỉ trụ sở tiếp nhận mới được xử lý"
        );
    }
}

    public void checkQuyenHoanThanh(TinHieuSOS sos, TruSo current) {
        if (sos.getIdTruSoTiepNhan() == null ||
            !sos.getIdTruSoTiepNhan().equals(current.getId())) {

            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ trụ sở tiếp nhận mới được hoàn thành SOS");
        }
    }


    public void checkTransition(String current, String next) {

        switch (current) {

            case "CHO_XU_LY":

    if (!(
            "DANG_XU_LY".equals(next)
            || "DA_HUY".equals(next)
    )) {

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Transition không hợp lệ"
        );
    }

    break;

            case "DANG_XU_LY":

    if (!(

            "HOAN_THANH".equals(next)
            || "DA_HUY".equals(next)

    )) {

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Transition không hợp lệ"
        );
    }

    break;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái không hợp lệ");
        }
    }
}
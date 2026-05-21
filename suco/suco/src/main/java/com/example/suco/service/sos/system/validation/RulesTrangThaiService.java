package com.example.suco.service.sos.system.validation;

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
        if (sos.getIdTruSoDeXuat() == null ||
            !sos.getIdTruSoDeXuat().equals(current.getId())) {

            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "SOS không thuộc về trụ sở của bạn");
        }
    }

    public void checkQuyenHoanThanh(TinHieuSOS sos, TruSo current) {
        if (sos.getIdTruSoTiepNhan() == null ||
            !sos.getIdTruSoTiepNhan().equals(current.getId())) {

            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ trụ sở tiếp nhận mới được hoàn thành SOS");
        }
    }

    public void checkQuyenTuChoi(TinHieuSOS sos, TruSo current) {
        boolean hopLe =
                (sos.getIdTruSoDeXuat() != null &&
                 sos.getIdTruSoDeXuat().equals(current.getId()))
                ||
                (sos.getIdTruSoTiepNhan() != null &&
                 sos.getIdTruSoTiepNhan().equals(current.getId()));

        if (!hopLe) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền từ chối SOS này");
        }
    }

    public void checkTransition(String current, String next) {

        switch (current) {

            case "CHO_XU_LY":
                if (!("DANG_XU_LY".equals(next) || "TU_CHOI".equals(next))) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transition không hợp lệ");
                }
                break;

            case "DANG_XU_LY":
                if (!"HOAN_THANH".equals(next)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transition không hợp lệ");
                }
                break;

            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái không hợp lệ");
        }
    }
}
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái mới không được để trống");
        }
    }

    public void checkDaKetThuc(String currentStatus) {
        if ("HOAN_THANH".equals(currentStatus) || 
            "DA_HUY".equals(currentStatus) || 
            "HUY_BO".equals(currentStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tín hiệu SOS này đã kết thúc xử lý");
        }
    }

    // Nới lỏng phân quyền: Nếu ca SOS tự động tiếp nhận chưa có Trụ sở, cho phép Trụ sở hiện tại gán ID vào luôn
    public void checkQuyenXuLy(TinHieuSOS sos, TruSo current) {
        if (sos.getIdTruSoTiepNhan() != null && !sos.getIdTruSoTiepNhan().equals(current.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Chỉ trụ sở tiếp nhận mới được quyền xử lý tín hiệu này"
            );
        }
    }

    public void checkQuyenHoanThanh(TinHieuSOS sos, TruSo current) {
        if (sos.getIdTruSoTiepNhan() == null || !sos.getIdTruSoTiepNhan().equals(current.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ trụ sở tiếp nhận mới được hoàn thành SOS");
        }
    }

    // Cập nhật lại luồng trạng thái chuẩn theo ý của Quân
    public void checkTransition(String current, String next) {
        switch (current) {
            case "DA_TIEP_NHAN":
                // Từ Đã tiếp nhận (Mặc định ban đầu) -> Bấm xuất phát chỉ được chuyển sang Chờ xử lý (hoặc Hủy)
                if (!("DANG_DI_CHUYEN".equals(next) || "DA_HUY".equals(next))) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Từ trạng thái Đã tiếp nhận phải chuyển sang Chờ xử lý cứu trợ");
                }
                break;

            case "DANG_DI_CHUYEN":
                // Từ Chờ xử lý (Đang di chuyển trên đường) -> Bấm bắt đầu cứu hộ để chuyển sang Đang xử lý
                if (!("DANG_XU_LY".equals(next) || "DA_HUY".equals(next))) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Từ trạng thái Chờ xử lý phải chuyển sang Đang xử lý cứu hộ");
                }
                break;

            case "DANG_XU_LY":
                // Từ Đang xử lý tại hiện trường -> Chuyển sang Hoàn thành
                if (!("HOAN_THANH".equals(next) || "DA_HUY".equals(next))) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Từ trạng thái Đang xử lý phải chuyển sang Hoàn thành");
                }
                break;

            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái hiện tại của hệ thống không hợp lệ: " + current);
        }
    }
}
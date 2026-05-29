package com.example.suco.mapper;

import com.example.suco.dto.tienich.qua.quanly.QuaRequestDTO;
import com.example.suco.dto.tienich.qua.quanly.QuaResponseDTO;
import com.example.suco.model.Qua;

public class QuaMapper {

    /**
     * Chuyển đổi từ Request DTO sang Entity (Dùng khi Thêm mới / Cập nhật)
     * Lưu ý: Trường hinhAnh (MultipartFile) sẽ được xử lý lưu file ở Service 
     * và gán chuỗi URL sau, nên ở đây tạm thời bỏ qua không map trực tiếp.
     */
    public static Qua toEntity(QuaRequestDTO requestDTO) {
        if (requestDTO == null) {
            return null;
        }

        Qua qua = new Qua();
        qua.setTen(requestDTO.getTen());
        qua.setLoai(requestDTO.getLoai());
        qua.setMoTa(requestDTO.getMoTa());
        qua.setDiem(requestDTO.getDiem());
        qua.setGiaTriGiamPercent(requestDTO.getGiaTriGiamPercent());
        qua.setGiaTriToiDa(requestDTO.getGiaTriToiDa());
        qua.setNgayKetThuc(requestDTO.getNgayKetThuc());
        
        // Nếu request không truyền trạng thái, mặc định là HOAT_DONG
        if (requestDTO.getTrangThai() != null) {
            qua.setTrangThai(requestDTO.getTrangThai());
        }

        return qua;
    }

    /**
     * Chuyển đổi từ Entity sang Response DTO (Dùng khi trả dữ liệu về cho Client công khai)
     */
    public static QuaResponseDTO toResponseDTO(Qua qua) {
        if (qua == null) {
            return null;
        }

        QuaResponseDTO responseDTO = new QuaResponseDTO();
        responseDTO.setId(qua.getId()); // Bắt buộc phải trả về ID cho Client
        responseDTO.setTen(qua.getTen());
        responseDTO.setLoai(qua.getLoai());
        responseDTO.setMoTa(qua.getMoTa());
        responseDTO.setDiem(qua.getDiem());
        responseDTO.setHinhAnh(qua.getHinhAnh()); // Trả về đường dẫn chuỗi (URL) của ảnh
        responseDTO.setGiaTriGiamPercent(qua.getGiaTriGiamPercent());
        responseDTO.setGiaTriToiDa(qua.getGiaTriToiDa());
        responseDTO.setNgayKetThuc(qua.getNgayKetThuc());
        responseDTO.setTrangThai(qua.getTrangThai());

        return responseDTO;
    }
}
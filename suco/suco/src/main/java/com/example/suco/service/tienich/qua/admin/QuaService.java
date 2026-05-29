package com.example.suco.service.tienich.qua.admin;

import com.example.suco.dto.tienich.qua.quanly.QuaRequestDTO;
import com.example.suco.dto.tienich.qua.quanly.QuaResponseDTO;
import com.example.suco.mapper.QuaMapper;
import com.example.suco.model.Qua;
import com.example.suco.repository.tienich.qua.QuaRepository;
import com.example.suco.service.tienich.qua.admin.validation.ValidateService;
import com.example.suco.service.tienich.qua.admin.storage.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class QuaService {

    private static final Logger log = LoggerFactory.getLogger(QuaService.class);

    @Autowired
    private QuaRepository quaRepository;

    @Autowired
private ValidateService validateService;

@Autowired
private FileStorageService fileStorageService;



    /**
     * Lấy tất cả quà
     */
    public List<QuaResponseDTO> getAllQua() {
        return quaRepository.findAll()
                .stream()
                .map(QuaMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy quà theo id
     */
    public QuaResponseDTO getQuaById(Long id) {
        Qua qua = quaRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy quà với id = " + id));

        return QuaMapper.toResponseDTO(qua);
    }

    /**
     * Tạo quà
     */
    public QuaResponseDTO createQua(QuaRequestDTO requestDTO) {

        log.info("===== Admin tạo quà =====");
        log.info("Tên: {}", requestDTO.getTen());
        log.info("Loại: {}", requestDTO.getLoai());
        log.info("Điểm: {}", requestDTO.getDiem());

        // Validate
        validateService.validateCreate(requestDTO);

        Qua qua = QuaMapper.toEntity(requestDTO);

        // Mặc định trạng thái
        if (qua.getTrangThai() == null) {
            qua.setTrangThai(Qua.TrangThai.HOAT_DONG);
        }

        // Upload ảnh
        String fileName = "default.png";

if (requestDTO.getHinhAnh() != null
        && !requestDTO.getHinhAnh().isEmpty()) {

    fileName = fileStorageService.saveFile(
            requestDTO.getHinhAnh());
}

        qua.setHinhAnh(fileName);

        // Logic Voucher
        if (requestDTO.getLoai() == Qua.LoaiQua.VOUCHER) {

            qua.setGiaTriGiamPercent(
                    requestDTO.getGiaTriGiamPercent());

            qua.setGiaTriToiDa(
                    requestDTO.getGiaTriToiDa());

        } else {

            qua.setGiaTriGiamPercent(null);
            qua.setGiaTriToiDa(null);
        }

        Qua savedQua = quaRepository.save(qua);

        log.info("Tạo quà thành công ID={}", savedQua.getId());

        return QuaMapper.toResponseDTO(savedQua);
    }

    /**
     * Cập nhật quà (PATCH)
     */
    public QuaResponseDTO updateQua(
            Long id,
            QuaRequestDTO requestDTO) {

        log.info("===== Cập nhật quà ID={} =====", id);

        Qua qua = quaRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy quà"));

        if (requestDTO.getTen() != null) {
            qua.setTen(requestDTO.getTen());
        }

        if (requestDTO.getMoTa() != null) {
            qua.setMoTa(requestDTO.getMoTa());
        }

        if (requestDTO.getLoai() != null) {
            qua.setLoai(requestDTO.getLoai());
        }

        validateService.validateUpdate(requestDTO);

if (requestDTO.getDiem() != null) {
    qua.setDiem(requestDTO.getDiem());
}

        if (requestDTO.getNgayKetThuc() != null) {
            qua.setNgayKetThuc(
                    requestDTO.getNgayKetThuc());
        }

        if (requestDTO.getTrangThai() != null) {
            qua.setTrangThai(
                    requestDTO.getTrangThai());
        }

        // Upload ảnh mới
        if (requestDTO.getHinhAnh() != null
        && !requestDTO.getHinhAnh().isEmpty()) {

    String fileName =
            fileStorageService.saveFile(
                    requestDTO.getHinhAnh());

    qua.setHinhAnh(fileName);
}

        // Logic voucher
        if (requestDTO.getLoai() != null) {

            if (requestDTO.getLoai()
                    == Qua.LoaiQua.VOUCHER) {

                if (requestDTO.getGiaTriGiamPercent()
                        != null) {

                    qua.setGiaTriGiamPercent(
                            requestDTO.getGiaTriGiamPercent());
                }

                if (requestDTO.getGiaTriToiDa()
                        != null) {

                    qua.setGiaTriToiDa(
                            requestDTO.getGiaTriToiDa());
                }

            } else {

                qua.setGiaTriGiamPercent(null);
                qua.setGiaTriToiDa(null);
            }
        }

        Qua updatedQua = quaRepository.save(qua);

        log.info("Cập nhật thành công ID={}", id);

        return QuaMapper.toResponseDTO(updatedQua);
    }

    /**
     * Xóa quà
     */
    public void deleteQua(Long id) {

        Qua qua = quaRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy quà với id = " + id));

        quaRepository.delete(qua);

        log.info("Đã xóa quà ID={}", id);
    }

    /**
     * Đổi trạng thái
     */
    public void updateStatus(
            Long id,
            Qua.TrangThai trangThai) {

        Qua qua = quaRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy quà"));

        qua.setTrangThai(trangThai);

        quaRepository.save(qua);

        log.info(
                "Đổi trạng thái quà ID={} -> {}",
                id,
                trangThai
        );
    }
}
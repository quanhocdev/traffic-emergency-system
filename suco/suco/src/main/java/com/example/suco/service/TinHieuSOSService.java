package com.example.suco.service;

import com.example.suco.dto.TinHieuSOSRequestDTO;
import com.example.suco.model.MuaGoi;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.TruSo;
import com.example.suco.repository.MuaGoiRepository;
import com.example.suco.repository.TinHieuSOSRepository;
import com.example.suco.service.DieuPhoiSOSService.ThongTinDieuPhoi;
import com.example.suco.util.GeocodingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import com.example.suco.service.sos.system.mapper.*;

import org.springframework.http.HttpStatus;
@Service
public class TinHieuSOSService {

    private static final Logger log = LoggerFactory.getLogger(TinHieuSOSService.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private TinHieuSOSRepository tinHieuSOSRepository;

    @Autowired
    private MuaGoiRepository muaGoiRepository;

    @Autowired
    private TruSoService truSoService;

    @Autowired
    private GeocodingUtil geocodingUtil;

    @Autowired
    private DieuPhoiSOSService dieuPhoiService;

    @Autowired
    private TinHieuMapper tinHieuMapper;

    @Transactional
public Map<String, Object> xuLyTinHieuSOS(String uid, TinHieuSOSRequestDTO dto) {

    // 1. Tạo SOS
    TinHieuSOS sos = new TinHieuSOS();
    sos.setUserId(uid);
    sos.setViDo(dto.getViDo());
    sos.setKinhDo(dto.getKinhDo());
    sos.setGhiChu(dto.getGhiChu());

    // 2. Xử lý địa chỉ
    if (dto.getDiaChi() != null && !dto.getDiaChi().isEmpty()) {
        sos.setDiaChi(dto.getDiaChi());
    } else {
        try {
            var addrMap = geocodingUtil.getAddressFromCoordinates(sos.getViDo(), sos.getKinhDo());
            sos.setDiaChi(geocodingUtil.formatAddress(addrMap));
        } catch (Exception e) {
            sos.setDiaChi("Yêu cầu cứu hộ tại: " + sos.getViDo() + ", " + sos.getKinhDo());
        }
    }

    // 3. File
    if (dto.getHinhAnhBase64() != null && !dto.getHinhAnhBase64().isEmpty()) {
        sos.setHinhAnh(saveBase64ToFile(dto.getHinhAnhBase64(), "sos_img"));
    }

    if (dto.getGhiAmBase64() != null && !dto.getGhiAmBase64().isEmpty()) {
        sos.setGhiAm(saveBase64ToFile(dto.getGhiAmBase64(), "sos_audio"));
    }

    // 4. Save lần 1
    sos.setTrangThai("CHO_XU_LY");
    TinHieuSOS sosDaLuu = tinHieuSOSRepository.save(sos);

    // 5. VIP check
    List<MuaGoi> listGoi = muaGoiRepository.findByUserId(uid);

    boolean laVip = listGoi.stream()
            .anyMatch(mg -> "ACTIVE".equalsIgnoreCase(mg.getTrangThai()));

    sosDaLuu.setIsVip(laVip);

    // 6. Điều phối
    ThongTinDieuPhoi thongTinDieuPhoi = dieuPhoiService.khoiTaoDieuPhoi(sosDaLuu);

    TruSo truSoGanNhat = null;

    if (thongTinDieuPhoi != null) {
        Long idTruSoDauTien = thongTinDieuPhoi.layIdTruSoHienTai();

        if (idTruSoDauTien != null) {
            truSoGanNhat = truSoService.timTruSoTheoId(idTruSoDauTien);
            sosDaLuu.setIdTruSoDeXuat(idTruSoDauTien);
        }
    }

    // 7. VIP auto assign
    if (laVip && truSoGanNhat != null) {

        sosDaLuu.setIdTruSoTiepNhan(truSoGanNhat.getId());
        sosDaLuu.setTrangThai("DANG_XU_LY");

        dieuPhoiService.danhDauDaTiepNhan(
                sosDaLuu.getId(),
                sosDaLuu.getIdTruSoTiepNhan()
        );

        // notify user
        messagingTemplate.convertAndSend(
                "/topic/user/" + uid + "/sos-status",
                Map.of(
                        "idSOS", sosDaLuu.getId(),
                        "trangThai", "DANG_XU_LY",
                        "message", "Trụ sở " + truSoGanNhat.getTenTruSo() + " đang đến"
                )
        );

        // send to trụ sở
        messagingTemplate.convertAndSend(
                "/topic/truso/" + sosDaLuu.getIdTruSoTiepNhan(),
                tinHieuMapper.mapToDTO(sosDaLuu)
        );

        // clear others
        if (thongTinDieuPhoi != null) {
            for (Long idTruSo : thongTinDieuPhoi.getDanhSachIdTruSo()) {
                if (!idTruSo.equals(sosDaLuu.getIdTruSoTiepNhan())) {

                    messagingTemplate.convertAndSend(
                            "/topic/truso/" + idTruSo + "/dieu-phoi",
                            Map.of(
                                    "loaiThongBao", "XOA_SOS",
                                    "idSos", sosDaLuu.getId(),
                                    "reason", "VIP_GHI_DE"
                            )
                    );
                }
            }
        }
    }

    // 8. Save cuối cùng
    sosDaLuu = tinHieuSOSRepository.save(sosDaLuu);

    // 9. WebSocket admin
    messagingTemplate.convertAndSend(
            "/topic/admin/sos",
            tinHieuMapper.mapToDTO(sosDaLuu)
    );

    // 10. refresh history
    messagingTemplate.convertAndSend(
            "/topic/user/" + uid + "/history",
            "REFRESH"
    );

    // 11. response
    Map<String, Object> result = new HashMap<>();
    result.put("sosData", tinHieuMapper.mapToDTO(sosDaLuu));
    result.put("truSoGanNhat", truSoGanNhat);
    result.put("thongTinDieuPhoi", thongTinDieuPhoi);

    return result;
}
    private String saveBase64ToFile(String base64Data, String prefix) {
        try {
            String extension = prefix.contains("audio") ? ".m4a" : ".jpg";
            String fileName = System.currentTimeMillis() + "_" + prefix + extension;
            
            Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads", "sos");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String base64Content = base64Data.contains(",") ? base64Data.split(",")[1] : base64Data;
            byte[] bytes = Base64.getDecoder().decode(base64Content);
            
            Files.write(uploadPath.resolve(fileName), bytes);
            return "/uploads/sos/" + fileName;
        } catch (IOException e) {
            log.error("Lỗi lưu file SOS: {}", e.getMessage());
            return null;
        }
    }
    
}
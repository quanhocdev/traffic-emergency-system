package com.example.suco.service.sos.system.process;

import com.example.suco.dto.TinHieuSOSRequestDTO;
    import com.example.suco.model.MuaGoi;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.TruSo;
import com.example.suco.repository.MuaGoiRepository;
import com.example.suco.repository.TinHieuSOSRepository;
import com.example.suco.service.DieuPhoiSOSService;
import com.example.suco.service.DieuPhoiSOSService.ThongTinDieuPhoi;
import com.example.suco.service.TruSoService;
import com.example.suco.service.sos.system.file.FileStorageService;
import com.example.suco.service.sos.system.mapper.TinHieuMapper;
import com.example.suco.service.sos.system.notification.TinHieuRealtimeService;
import com.example.suco.util.GeocodingUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ProcessingService {

    @Autowired
    private TinHieuSOSRepository tinHieuSOSRepository;

    @Autowired
    private MuaGoiRepository muaGoiRepository;

    @Autowired
    private GeocodingUtil geocodingUtil;

    @Autowired
    private DieuPhoiSOSService dieuPhoiService;

    @Autowired
    private TruSoService truSoService;

    @Autowired
    private TinHieuMapper tinHieuMapper;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
private FileStorageService fileStorageService;

@Autowired
private TinHieuRealtimeService realtimeService;

    @Transactional
public Map<String, Object> xuLyTinHieuSOS(String uid, TinHieuSOSRequestDTO dto) {

    TinHieuSOS sos = createSOS(uid, dto);

    handleAddress(sos, dto);

    handleFiles(sos, dto);

    sos = tinHieuSOSRepository.save(sos);

    boolean laVip = checkVip(uid);
    sos.setIsVip(laVip);

    ThongTinDieuPhoi dp = dieuPhoiService.khoiTaoDieuPhoi(sos);

    TruSo truSo = resolveTruSo(sos, dp);

    handleVipFlow(laVip, sos, truSo, uid);

    sos = tinHieuSOSRepository.save(sos);

    notifySystem(sos, uid);

    return buildResponse(sos, truSo, dp);
}
private TinHieuSOS createSOS(String uid, TinHieuSOSRequestDTO dto) {
    TinHieuSOS sos = new TinHieuSOS();
    sos.setUserId(uid);
    sos.setViDo(dto.getViDo());
    sos.setKinhDo(dto.getKinhDo());
    sos.setGhiChu(dto.getGhiChu());
    sos.setTrangThai("CHO_XU_LY");
    return sos;
}
private void handleAddress(TinHieuSOS sos, TinHieuSOSRequestDTO dto) {
    if (dto.getDiaChi() != null && !dto.getDiaChi().isEmpty()) {
        sos.setDiaChi(dto.getDiaChi());
        return;
    }

    try {
        var addr = geocodingUtil.getAddressFromCoordinates(
                sos.getViDo(), sos.getKinhDo()
        );
        sos.setDiaChi(geocodingUtil.formatAddress(addr));
    } catch (Exception e) {
        sos.setDiaChi("SOS: " + sos.getViDo() + ", " + sos.getKinhDo());
    }
}
private void handleFiles(TinHieuSOS sos, TinHieuSOSRequestDTO dto) {
    if (dto.getHinhAnhBase64() != null) {
        sos.setHinhAnh(fileStorageService.saveBase64ToFile(dto.getHinhAnhBase64(), "sos_img"));
    }

    if (dto.getGhiAmBase64() != null) {
        sos.setGhiAm(fileStorageService.saveBase64ToFile(dto.getGhiAmBase64(), "sos_audio"));
    }
}
private boolean checkVip(String uid) {
    return muaGoiRepository.findByUserId(uid)
            .stream()
            .anyMatch(mg -> "ACTIVE".equalsIgnoreCase(mg.getTrangThai()));
}
private TruSo resolveTruSo(TinHieuSOS sos, ThongTinDieuPhoi dp) {
    if (dp == null || dp.layIdTruSoHienTai() == null) return null;

    Long id = dp.layIdTruSoHienTai();

    TruSo truSo = truSoService.timTruSoTheoId(id);
    sos.setIdTruSoDeXuat(id);

    return truSo;
}
private Map<String, Object> buildResponse(TinHieuSOS sos, TruSo truSo, ThongTinDieuPhoi dp) {
    Map<String, Object> result = new HashMap<>();
    result.put("sosData", tinHieuMapper.mapToDTO(sos));
    result.put("truSoGanNhat", truSo);
    result.put("thongTinDieuPhoi", dp);
    return result;
}
private void handleVipFlow(boolean laVip, TinHieuSOS sos, TruSo truSo, String uid) {

    if (!laVip || truSo == null) return;

    sos.setIdTruSoTiepNhan(truSo.getId());
    sos.setTrangThai("DANG_XU_LY");

    dieuPhoiService.danhDauDaTiepNhan(
            sos.getId(),
            truSo.getId()
    );
    realtimeService.guiThongDiep(sos);
}
private void notifySystem(TinHieuSOS sos, String uid) {
    realtimeService.realtimeGuiSOS(sos);
}
}
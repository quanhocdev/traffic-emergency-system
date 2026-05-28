package com.example.suco.service.sos.tinhieu.user;

import com.example.suco.dto.sos.tinhieu.TinHieuSOSRequestDTO;
import com.example.suco.dto.sos.tinhieu.TinHieuSOSResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.TruSo;
import com.example.suco.repository.sos.tinhieu.TinHieuSOSRepository;
import com.example.suco.service.dieuphoi.engine.DispatchEngineService;
import com.example.suco.service.location.GeocodingService;
import com.example.suco.service.sos.tinhieu.user.workflow.gui.builder.SosResponseBuilder;
import com.example.suco.service.sos.tinhieu.user.workflow.gui.create.CreateService;
import com.example.suco.service.sos.tinhieu.user.workflow.gui.file.FileStorageService;
import com.example.suco.service.sos.tinhieu.user.workflow.gui.resolver.TruSoResolver;
import com.example.suco.service.sos.tinhieu.user.workflow.gui.vip.VipService;

import java.util.Map;
@Service
public class GuiTinHieuService {

     @Autowired
    private TinHieuSOSRepository tinHieuSOSRepository;
       
    @Autowired
    private VipService vipService;
        
    @Autowired
    private CreateService createService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private GeocodingService geocodingService;

    @Autowired
    private DispatchEngineService dispatchEngineService;

    @Autowired
    private TruSoResolver truSoResolver;

    @Autowired
    private SosResponseBuilder sosResponseBuilder;

    public Map<String, Object> submitSOS(String uid, TinHieuSOSRequestDTO dto) {

    TinHieuSOS sos = createService.createSOS(uid, dto);

    // Nếu người dùng đã cung cấp địa chỉ, ưu tiên sử dụng địa chỉ đó
    if (dto.getDiaChi() != null && !dto.getDiaChi().isBlank()) {
        sos.setDiaChi(dto.getDiaChi());
    } else {
    sos.setDiaChi(
            // Nếu không có địa chỉ, sử dụng geocoding để lấy địa chỉ từ tọa độ
            geocodingService.getAddress(
                    sos.getViDo(),
                    sos.getKinhDo()
            )
    );
    }

    fileStorageService.handleFiles(sos, dto);

    sos = tinHieuSOSRepository.save(sos);

    boolean laVip = vipService.checkVip(uid);
    sos.setIsVip(laVip);

    sos = tinHieuSOSRepository.save(sos);

    dispatchEngineService.startDispatch(sos);

    sos = tinHieuSOSRepository.save(sos);

    TruSo truSo = truSoResolver.resolve(sos);

    vipService.handleVipFlow(laVip, sos, truSo, uid);

    sos = tinHieuSOSRepository.save(sos);

    return sosResponseBuilder.buildSosDto(sos, truSo);
}
}
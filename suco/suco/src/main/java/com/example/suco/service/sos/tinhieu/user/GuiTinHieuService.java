package com.example.suco.service.sos.tinhieu.user;

import com.example.suco.dto.sos.hoadon.quanly.TruSoMiniDTO;
import com.example.suco.dto.sos.tinhieu.GuiTinHieuResponseDTO;
import com.example.suco.dto.sos.tinhieu.TinHieuSOSRequestDTO;
import com.example.suco.mapper.TinHieuMapper;
import com.example.suco.repository.vanhanh.TruSoRepository;
import com.example.suco.repository.vanhanh.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.TruSo;
import com.example.suco.repository.sos.tinhieu.TinHieuSOSRepository;
import com.example.suco.service.dieuphoi.engine.DispatchEngineService;
import com.example.suco.service.location.GeocodingService;
import com.example.suco.service.sos.tinhieu.user.workflow.gui.file.StorageSOSService;
import com.example.suco.service.sos.tinhieu.user.workflow.gui.vip.VipService;
import com.example.suco.model.User;

@Service
public class GuiTinHieuService {

     @Autowired
    private TinHieuSOSRepository tinHieuSOSRepository;
       
    @Autowired
    private VipService vipService;
 
    @Autowired
    private StorageSOSService fileStorageService;

    @Autowired
    private GeocodingService geocodingService;

    @Autowired
    private DispatchEngineService dispatchEngineService;

    @Autowired
private TinHieuMapper tinHieuMapper;

@Autowired
private UserRepository userRepository;

@Autowired
private TruSoRepository truSoRepository;


public GuiTinHieuResponseDTO submitSOS(String uid, TinHieuSOSRequestDTO dto) {

    User user = userRepository.findByUid(uid).orElse(null);
    TinHieuSOS sos = tinHieuMapper.toEntity(dto, uid, user);

    // 1. địa chỉ
    if (dto.getDiaChi() != null && !dto.getDiaChi().isBlank()) {
        sos.setDiaChi(dto.getDiaChi());
    } else {
        sos.setDiaChi(
                geocodingService.getAddress(sos.getViDo(), sos.getKinhDo())
        );
    }

    // 2. file
    fileStorageService.handleFiles(sos, dto);

    // 3. VIP flag (chỉ metadata)
    boolean laVip = vipService.checkVip(uid);
    sos.setIsVip(laVip);

    // 4. DISPATCH (QUAN TRỌNG)
    dispatchEngineService.startDispatch(sos);

    // 5. SAVE DUY NHẤT
    sos = tinHieuSOSRepository.save(sos);

    TruSoMiniDTO truSoDTO = null;

if (sos.getIdTruSoTiepNhan() != null) {

    TruSo truSo = truSoRepository.findById(sos.getIdTruSoTiepNhan())
            .orElse(null);

    if (truSo != null) {
        truSoDTO = new TruSoMiniDTO();
        truSoDTO.setId(truSo.getId());
        truSoDTO.setTenTruSo(truSo.getTenTruSo());
    }
}


   return new GuiTinHieuResponseDTO(
        tinHieuMapper.mapToDTO(sos),
        truSoDTO
);
}
}
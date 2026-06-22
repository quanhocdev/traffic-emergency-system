package com.example.suco.service.sos.tinhieu.user;

import com.example.suco.dto.sos.tinhieu.user.TinHieuSOSRequestDTO;
import com.example.suco.mapper.TinHieuMapper;
import com.example.suco.repository.vanhanh.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.repository.sos.tinhieu.TinHieuSOSRepository;
import com.example.suco.service.dieuphoi.DispatchEngineService;
import com.example.suco.service.location.GeocodingService;
import com.example.suco.service.sos.tinhieu.user.workflow.gui.StorageSOSService;
import com.example.suco.service.sos.tinhieu.user.workflow.gui.VipService;
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

public Long submitSOS(String uid, TinHieuSOSRequestDTO dto) {

    User user = userRepository.findByUid(uid).orElse(null);

    TinHieuSOS sos = tinHieuMapper.toEntity(dto, uid, user);

    sos.setDiaChi(
            geocodingService.getAddress(
                    sos.getViDo(),
                    sos.getKinhDo()
            )
    );

    fileStorageService.handleFiles(sos, dto);

    sos.setIsVip(vipService.checkVip(uid));

    dispatchEngineService.startDispatch(sos);

    sos = tinHieuSOSRepository.save(sos);

    return sos.getId();
}
}
package com.example.suco.service.sos.tinhieu.user;

import com.example.suco.dto.sos.tinhieu.user.TinHieuSOSRequestDTO;
import com.example.suco.mapper.TinHieuMapper;
import com.example.suco.repository.vanhanh.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.repository.sos.tinhieu.TinHieuSOSRepository;
import com.example.suco.service.VipService;
import com.example.suco.service.dieuphoi.DispatchEngineService;
import com.example.suco.service.file.FileStorageService;
import com.example.suco.service.location.GeocodingService;
import com.example.suco.model.User;

@Service
public class GuiTinHieuService {

 @Autowired
    private TinHieuSOSRepository tinHieuSOSRepository;
       
    @Autowired
    private VipService vipService;
 
    @Autowired
private FileStorageService fileStorageService;

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

    if (dto.getHinhAnhBase64() != null) {
    sos.setHinhAnh(
            fileStorageService.saveBase64(
                    dto.getHinhAnhBase64(),
                    "sos",
                    "sos_img",
                    ".jpg"
            )
    );
}

if (dto.getGhiAmBase64() != null) {
    sos.setGhiAm(
            fileStorageService.saveBase64(
                    dto.getGhiAmBase64(),
                    "sos",
                    "sos_audio",
                    ".m4a"
            )
    );
}

    sos.setIsVip(vipService.checkVip(uid));

    dispatchEngineService.startDispatch(sos);

    sos = tinHieuSOSRepository.save(sos);

    return sos.getId();
}
}
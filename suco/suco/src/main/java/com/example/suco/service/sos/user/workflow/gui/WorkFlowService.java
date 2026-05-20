package com.example.suco.service.sos.user.workflow.gui;

import com.example.suco.dto.TinHieuSOSRequestDTO;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.TruSo;
import com.example.suco.repository.TinHieuSOSRepository;
import com.example.suco.service.DieuPhoiSOSService;
import com.example.suco.service.DieuPhoiSOSService.ThongTinDieuPhoi;
import com.example.suco.service.TruSoService;
import com.example.suco.service.sos.system.mapper.TinHieuMapper;
import com.example.suco.service.sos.user.workflow.gui.file.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.suco.service.sos.user.workflow.gui.vip.VipService;
import com.example.suco.service.sos.user.workflow.gui.create.CreateService;
import com.example.suco.service.sos.user.workflow.gui.address.AddressService;

import java.util.*;

@Service
public class WorkFlowService {

    @Autowired
    private TinHieuSOSRepository tinHieuSOSRepository;


    @Autowired
    private DieuPhoiSOSService dieuPhoiService;

    @Autowired
    private TruSoService truSoService;

    @Autowired
    private TinHieuMapper tinHieuMapper;

   

        @Autowired
        private VipService vipService;

        @Autowired
        private CreateService create;

        @Autowired
        private FileStorageService fileStorageService;

        @Autowired
        private AddressService addressService;


    @Transactional
public Map<String, Object> xuLyTinHieuSOS(String uid, TinHieuSOSRequestDTO dto) {

    TinHieuSOS sos = create.createSOS(uid, dto);

    addressService.handleAddress(sos, dto);

    fileStorageService.handleFiles(sos, dto);

    sos = tinHieuSOSRepository.save(sos);

    boolean laVip = vipService.checkVip(uid);
    sos.setIsVip(laVip);

    ThongTinDieuPhoi dp = dieuPhoiService.khoiTaoDieuPhoi(sos);

    TruSo truSo = resolveTruSo(sos, dp);

    vipService.handleVipFlow(laVip, sos, truSo, uid);

    sos = tinHieuSOSRepository.save(sos);

    return buildResponse(sos, truSo, dp);
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

}
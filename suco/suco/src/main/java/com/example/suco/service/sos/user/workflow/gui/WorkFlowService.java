package com.example.suco.service.sos.user.workflow.gui;

import com.example.suco.dto.TinHieuSOSRequestDTO;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.TruSo;
import com.example.suco.repository.TinHieuSOSRepository;
import com.example.suco.service.DieuPhoiSOSService;
import com.example.suco.service.TruSoService;
import com.example.suco.service.sos.system.mapper.TinHieuMapper;
import com.example.suco.service.sos.user.workflow.gui.file.FileStorageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.suco.service.sos.user.workflow.gui.vip.VipService;
import com.example.suco.service.sos.user.workflow.gui.create.CreateService;
import com.example.suco.service.sos.user.workflow.gui.address.AddressService;
import com.example.suco.service.sos.user.workflow.gui.mapper.SosResponseBuilder;
import com.example.suco.service.sos.user.workflow.gui.resolver.TruSoResolver;

import com.example.suco.service.dieuphoi.engine.DispatchEngineService;


import java.util.*;

@Service
public class WorkFlowService {

    @Autowired
    private TinHieuSOSRepository tinHieuSOSRepository;
       
    @Autowired
    private VipService vipService;
        
    @Autowired
    private CreateService create;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private DispatchEngineService dispatchEngineService;

    @Autowired
    private SosResponseBuilder sosResponseBuilder;

    @Autowired
    private TruSoResolver truSoResolver;


    @Transactional
    public Map<String, Object> xuLyTinHieuSOS(String uid, TinHieuSOSRequestDTO dto) {

    TinHieuSOS sos = create.createSOS(uid, dto);

    addressService.handleAddress(sos, dto);

    fileStorageService.handleFiles(sos, dto);

    sos = tinHieuSOSRepository.save(sos);

    boolean laVip = vipService.checkVip(uid);
    sos.setIsVip(laVip);
    sos = tinHieuSOSRepository.save(sos);

    dispatchEngineService.startDispatch(sos);

    TruSo truSo = truSoResolver.resolve(sos);

    vipService.handleVipFlow(laVip, sos, truSo, uid);

    sos = tinHieuSOSRepository.save(sos);

    return sosResponseBuilder.build(sos, truSo);
}

}
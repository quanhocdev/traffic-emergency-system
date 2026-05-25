package com.example.suco.service.sos.tinhieu.user.workflow.gui.builder;

import com.example.suco.mapper.TinHieuMapper;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.TruSo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SosResponseBuilder {

    @Autowired
    private TinHieuMapper tinHieuMapper;

    public Map<String, Object> buildSosDto(TinHieuSOS sos, TruSo truSo) {

    Map<String, Object> result = new HashMap<>();

    result.put("entity", sos);

    result.put("sosData", tinHieuMapper.mapToDTO(sos));

    result.put("truSoGanNhat", truSo);

    return result;
}
}
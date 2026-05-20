package com.example.suco.service.sos.user.workflow.gui.mapper;

import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.TruSo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.suco.service.sos.system.mapper.TinHieuMapper;
import java.util.HashMap;
import java.util.Map;

@Service
public class SosResponseBuilder {

    @Autowired
    private TinHieuMapper tinHieuMapper;

    public Map<String, Object> build(TinHieuSOS sos, TruSo truSo) {

        Map<String, Object> result = new HashMap<>();
        result.put("sosData", tinHieuMapper.mapToDTO(sos));
        result.put("truSoGanNhat", truSo);

        return result;
    }
}
package com.example.suco.service.sos.tinhieu.user.workflow.gui.resolver;
import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.TruSo;
import com.example.suco.service.xacthuc.truso.TruSoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class TruSoResolver {

    @Autowired
    private TruSoService truSoService;

    public TruSo resolve(TinHieuSOS sos) {

        Long id = sos.getIdTruSoDeXuat();
        if (id == null) return null;

        return truSoService.timTruSoTheoId(id);
    }
}
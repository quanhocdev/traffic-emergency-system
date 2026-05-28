package com.example.suco.service.sos.tinhieu.truso.validation;

import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.TruSo;
import com.example.suco.service.sos.tinhieu.truso.validation.rules.RulesTrangThaiService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CheckTrangThaiService {

    @Autowired
    private RulesTrangThaiService rules;

    public void validateAll(
            TinHieuSOS sos,
            String currentStatus,
            String newStatus,
            TruSo current
    ) {

        rules.checkNotNull(sos);

        rules.checkStatusHopLe(newStatus);

        rules.checkDaKetThuc(currentStatus);

        rules.checkTransition(currentStatus, newStatus);

        switch (newStatus) {

            case "DANG_XU_LY":

                rules.checkQuyenXuLy(sos, current);

                break;

            case "HOAN_THANH":

                rules.checkQuyenHoanThanh(sos, current);

                break;

            case "DA_HUY":

                rules.checkQuyenXuLy(sos, current);

                break;
        }
    }
}
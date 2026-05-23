package com.example.suco.service.sos.system.validation;

import com.example.suco.model.TinHieuSOS;
import com.example.suco.model.TruSo;
import com.example.suco.service.dieuphoi.rules.RulesTrangThaiService;

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

            case "TIEP_NHAN":
                rules.checkQuyenXuLy(sos, current);
                break;

            case "DANG_XU_LY":
                rules.checkQuyenXuLy(sos, current);
                break;

            case "HOAN_THANH":
                rules.checkQuyenHoanThanh(sos, current);
                break;

            case "TU_CHOI":
                rules.checkQuyenTuChoi(sos, current);
                break;

            case "TIMEOUT":
                break;
        }
    }
}
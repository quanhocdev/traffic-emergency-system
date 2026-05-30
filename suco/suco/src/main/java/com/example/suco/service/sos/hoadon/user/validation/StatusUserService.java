package com.example.suco.service.sos.hoadon.user.validation;

import com.example.suco.model.HoaDon;

import org.springframework.stereotype.Service;

@Service
public class StatusUserService {

    public void validateHoaDon(
            HoaDon hd,
            String uid
    ) {

        if (
                hd.getUserId() == null
                ||
                !hd.getUserId().equals(uid)
        ) {

            throw new RuntimeException(
                    "Bạn không có quyền thanh toán hóa đơn này"
            );
        }
    }
}
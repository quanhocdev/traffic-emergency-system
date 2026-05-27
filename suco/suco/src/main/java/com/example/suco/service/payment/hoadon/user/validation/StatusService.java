package com.example.suco.service.payment.hoadon.user.validation;

import com.example.suco.model.HoaDon;

import org.springframework.stereotype.Service;

@Service
public class StatusService {

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
package com.example.suco.service.payment.hoadon.validation.total;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TotalAmountService {

    public BigDecimal tinhTongTien(
            BigDecimal thanhTien,
            BigDecimal soTienGiam
    ) {

        BigDecimal tong =
                thanhTien.subtract(soTienGiam);

        if (tong.compareTo(BigDecimal.ZERO) < 0) {
            tong = BigDecimal.ZERO;
        }

        return tong;
    }
}
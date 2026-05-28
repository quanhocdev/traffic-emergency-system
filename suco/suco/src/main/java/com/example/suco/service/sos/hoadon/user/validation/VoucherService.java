package com.example.suco.service.sos.hoadon.user.validation;

import com.example.suco.model.HoaDon;
import com.example.suco.model.Qua;
import com.example.suco.repository.tienich.qua.DoiQuaRepository;
import com.example.suco.repository.tienich.qua.QuaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class VoucherService {

    @Autowired
    private QuaRepository quaRepository;

    @Autowired
    private DoiQuaRepository doiQuaRepository;

    public BigDecimal apDungVoucher(
            HoaDon hd,
            Long quaId
    ) {

        if (quaId == null) {
            return BigDecimal.ZERO;
        }

        Qua voucher = quaRepository.findById(quaId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Voucher không tồn tại"
                        )
                );

        if (voucher.getLoai() != Qua.LoaiQua.VOUCHER) {
            throw new RuntimeException(
                    "Vật phẩm này không phải Voucher"
            );
        }

        BigDecimal giaGoc = hd.getThanhTien();

        BigDecimal phanTram =
                BigDecimal.valueOf(
                        voucher.getGiaTriGiamPercent()
                ).divide(new BigDecimal(100));

        BigDecimal soTienGiam =
                giaGoc.multiply(phanTram);

        if (
                voucher.getGiaTriToiDa() != null
                &&
                soTienGiam.compareTo(
                        voucher.getGiaTriToiDa()
                ) > 0
        ) {

            soTienGiam =
                    voucher.getGiaTriToiDa();
        }

        doiQuaRepository
                .findByUserIdAndQuaId(
                        hd.getUserId(),
                        quaId
                )
                .ifPresent(dq -> {

                    if (
                            dq.getSoLuong() != null
                            &&
                            dq.getSoLuong() > 1
                    ) {

                        dq.setSoLuong(
                                dq.getSoLuong() - 1
                        );

                        doiQuaRepository.save(dq);

                    } else {

                        doiQuaRepository.delete(dq);
                    }
                });

        return soTienGiam;
    }
}
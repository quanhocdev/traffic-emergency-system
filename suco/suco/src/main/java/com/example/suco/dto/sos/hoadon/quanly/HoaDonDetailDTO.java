package com.example.suco.dto.sos.hoadon.quanly;

import java.util.List;
import com.example.suco.dto.sos.hoadon.payment.ThanhToanResponseDTO;

public class HoaDonDetailDTO {

    private HoaDonResponseDTO hoaDon;

    private List<ThanhToanResponseDTO> thanhToans;

    public HoaDonResponseDTO getHoaDon() {
        return hoaDon;
    }

    public void setHoaDon(HoaDonResponseDTO hoaDon) {
        this.hoaDon = hoaDon;
    }

    public List<ThanhToanResponseDTO> getThanhToans() {
        return thanhToans;
    }

    public void setThanhToans(List<ThanhToanResponseDTO> thanhToans) {
        this.thanhToans = thanhToans;
    }
}
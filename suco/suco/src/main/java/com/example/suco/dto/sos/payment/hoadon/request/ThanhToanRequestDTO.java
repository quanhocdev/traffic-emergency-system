package com.example.suco.dto.sos.payment.hoadon.request;

public class ThanhToanRequestDTO {

    private Long hoaDonId;
    private Long quaId;
    private String phuongThucThanhToan;

    public Long getHoaDonId() {
        return hoaDonId;
    }
    public void setHoaDonId(Long hoaDonId) {
        this.hoaDonId = hoaDonId;
    }
    public Long getQuaId() {
        return quaId;
    }
    public void setQuaId(Long quaId) {
        this.quaId = quaId;
    }
    public String getPhuongThucThanhToan() {
        return phuongThucThanhToan;
    }
    public void setPhuongThucThanhToan(String phuongThucThanhToan) {
        this.phuongThucThanhToan = phuongThucThanhToan;
    }
}
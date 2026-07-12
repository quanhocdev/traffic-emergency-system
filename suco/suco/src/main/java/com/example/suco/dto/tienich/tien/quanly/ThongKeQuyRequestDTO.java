package com.example.suco.dto.tienich.tien.quanly;
import java.time.LocalDate;

public class ThongKeQuyRequestDTO {

    private LocalDate tuNgay;

    private LocalDate denNgay;


    public LocalDate getTuNgay() {
        return tuNgay;
    }

    public void setTuNgay(LocalDate tuNgay) {
        this.tuNgay = tuNgay;
    }


    public LocalDate getDenNgay() {
        return denNgay;
    }

    public void setDenNgay(LocalDate denNgay) {
        this.denNgay = denNgay;
    }
}

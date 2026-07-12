package com.example.suco.mapper;


import com.example.suco.dto.tienich.tien.quanly.VinhDanhDTO;
import org.springframework.stereotype.Component;


@Component
public class VinhDanhMapper {


    public VinhDanhDTO toDTO(
            String tenHienThi,
            Long tongDongGop
    ){

        return new VinhDanhDTO(
                tenHienThi,
                tongDongGop
        );
    }

}
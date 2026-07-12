package com.example.suco.mapper;

import com.example.suco.dto.tienich.tien.quyengop.QuyenGopRequestDTO;
import com.example.suco.dto.tienich.tien.quyengop.QuyenGopResponseDTO;
import com.example.suco.model.QuyenGop;
import org.springframework.stereotype.Component;


@Component
public class QuyenGopMapper {


    public QuyenGop toEntity(QuyenGopRequestDTO request){

        QuyenGop entity = new QuyenGop();

        entity.setSoDiemQuyenGop(
            request.getSoDiemQuyenGop()
        );

        entity.setNoiDung(
            request.getNoiDung()
        );

        return entity;
    }



    public QuyenGopResponseDTO toResponseDTO(
            QuyenGop entity
    ){

        QuyenGopResponseDTO dto =
                new QuyenGopResponseDTO();


        dto.setId(entity.getId());

        dto.setSoDiemQuyenGop(
            entity.getSoDiemQuyenGop()
        );

        dto.setGiaTri(
            entity.getGiaTri()
        );

        dto.setNoiDung(
            entity.getNoiDung()
        );

        dto.setNgayQuyenGop(
            entity.getNgayQuyenGop()
        );


        return dto;
    }
}
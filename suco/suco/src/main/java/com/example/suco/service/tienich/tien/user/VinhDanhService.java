package com.example.suco.service.tienich.tien.user;


import com.example.suco.dto.tienich.tien.quanly.ThongKeQuyResponseDTO;
import com.example.suco.dto.tienich.tien.quanly.VinhDanhDTO;
import com.example.suco.repository.tienich.tien.VinhDanhRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;



@Service
public class VinhDanhService {



    @Autowired
    private VinhDanhRepository vinhDanhRepository;




    public ThongKeQuyResponseDTO getThongKe(){

        Long tong =
                vinhDanhRepository.sumTongQuyenGop();


        if(tong == null){
            tong = 0L;
        }


        List<VinhDanhDTO> bangVinhDanh =
                vinhDanhRepository.findBangVinhDanh();



        return new ThongKeQuyResponseDTO(
                tong,
                bangVinhDanh
        );
    }

}
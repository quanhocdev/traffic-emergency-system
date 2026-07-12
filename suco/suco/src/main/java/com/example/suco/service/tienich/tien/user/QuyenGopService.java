package com.example.suco.service.tienich.tien.user;


import com.example.suco.dto.tienich.tien.quyengop.QuyenGopRequestDTO;
import com.example.suco.dto.tienich.tien.quyengop.QuyenGopResponseDTO;
import com.example.suco.mapper.QuyenGopMapper;
import com.example.suco.model.QuyenGop;
import com.example.suco.model.User;
import com.example.suco.repository.tienich.tien.QuyenGopRepository;
import com.example.suco.repository.vanhanh.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;



@Service
public class QuyenGopService {


    @Autowired
    private QuyenGopRepository quyenGopRepository;


    @Autowired
    private UserRepository userRepository;


    @Autowired
    private QuyenGopMapper quyenGopMapper;



    private final long HE_SO = 100L;



    @Transactional
    public boolean thucHienQuyenGop(
            String uid,
            QuyenGopRequestDTO dto
    ){


        if(dto.getSoDiemQuyenGop() <= 0)
            return false;



        User user =
                userRepository.findById(uid)
                .orElse(null);



        if(user == null ||
           user.getTotalPoints() < dto.getSoDiemQuyenGop())
            return false;



        // trừ điểm

        user.setTotalPoints(
                user.getTotalPoints()
                - dto.getSoDiemQuyenGop()
        );


        userRepository.save(user);



        long giaTri =
                dto.getSoDiemQuyenGop() * HE_SO;



        QuyenGop quyenGop =
                quyenGopMapper.toEntity(dto);



        quyenGop.setUser(user);

        quyenGop.setGiaTri(giaTri);

        quyenGop.setNgayQuyenGop(
                LocalDateTime.now()
        );


        quyenGopRepository.save(quyenGop);



        return true;
    }





    public List<QuyenGopResponseDTO> getLichSu(
            String uid
    ){

        return quyenGopRepository
                .findByUserUid(uid)
                .stream()
                .map(quyenGopMapper::toResponseDTO)
                .collect(Collectors.toList());

    }

}
package com.example.suco.service.tienich.tien.user;


import com.example.suco.dto.tienich.tien.quydoi.DoiTienRequestDTO;
import com.example.suco.dto.tienich.tien.quydoi.DoiTienResponseDTO;
import com.example.suco.mapper.tien.DoiTienMapper;
import com.example.suco.model.DoiTien;
import com.example.suco.model.User;
import com.example.suco.repository.tienich.tien.DoiTienRepository;
import com.example.suco.repository.vanhanh.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class DoiTienService {


    @Autowired
    private DoiTienRepository doiTienRepository;


    @Autowired
    private UserRepository userRepository;


    @Autowired
    private DoiTienMapper doiTienMapper;



    private final long HE_SO = 100L;



    @Transactional
    public boolean thucHienDoiTien(
            String uid,
            DoiTienRequestDTO dto
    ){

        if(dto.getSoDiemDoi() <= 0)
            return false;


        User user = userRepository
                .findById(uid)
                .orElse(null);


        if(user == null ||
           user.getTotalPoints() < dto.getSoDiemDoi())
            return false;



        // trừ điểm

        user.setTotalPoints(
                user.getTotalPoints()
                - dto.getSoDiemDoi()
        );


        userRepository.save(user);



        long giaTri =
                dto.getSoDiemDoi() * HE_SO;



        DoiTien doiTien =
                doiTienMapper.toEntity(dto);



        doiTien.setUser(user);

        doiTien.setGiaTri(giaTri);

        doiTien.setNgayDoi(
                LocalDateTime.now()
        );


        doiTienRepository.save(doiTien);


        return true;
    }





    public List<DoiTienResponseDTO> getLichSu(
            String uid
    ){

        return doiTienRepository
                .findByUserUid(uid)
                .stream()
                .map(doiTienMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

}
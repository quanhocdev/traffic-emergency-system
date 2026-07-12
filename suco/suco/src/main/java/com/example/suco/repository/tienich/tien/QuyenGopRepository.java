package com.example.suco.repository.tienich.tien;

import com.example.suco.model.QuyenGop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface QuyenGopRepository 
        extends JpaRepository<QuyenGop, Long> {


    // lịch sử quyên góp của user
    List<QuyenGop> findByUserUid(String uid);

}
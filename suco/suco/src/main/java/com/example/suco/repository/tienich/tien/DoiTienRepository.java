package com.example.suco.repository.tienich.tien;

import com.example.suco.model.DoiTien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface DoiTienRepository 
        extends JpaRepository<DoiTien, Long> {


    List<DoiTien> findByUserUid(String uid);

}
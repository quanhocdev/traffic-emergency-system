package com.example.suco.repository.tienich.qua;

import com.example.suco.model.DoiQua;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoiQuaRepository extends JpaRepository<DoiQua, Long> {

}
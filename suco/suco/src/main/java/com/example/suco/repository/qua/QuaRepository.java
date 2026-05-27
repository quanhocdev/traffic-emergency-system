package com.example.suco.repository.qua;

import com.example.suco.model.Qua;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface QuaRepository extends JpaRepository<Qua, Long> {
    List<Qua> findByTrangThai(Qua.TrangThai trangThai);
}
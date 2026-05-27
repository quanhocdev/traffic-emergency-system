package com.example.suco.repository.sos.goi;

import com.example.suco.model.Goi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CRUDGoiRepository extends JpaRepository<Goi, Long> {
}
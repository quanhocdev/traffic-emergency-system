package com.example.suco.repository.suco.baocao;

import com.example.suco.model.Spam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpamRepository extends JpaRepository<Spam, Long> {
}
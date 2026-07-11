package com.example.suco.repository.tienich.qua;

import com.example.suco.model.TuiQua;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TuiQuaRepository extends JpaRepository<TuiQua, Long> {

    Optional<TuiQua> findByUserIdAndQuaId(String userId, Long quaId);

    @Query("""
        SELECT t
        FROM TuiQua t
        JOIN FETCH t.qua
        WHERE t.userId = :uid
    """)
    List<TuiQua> findByUserId(@Param("uid") String uid);
}
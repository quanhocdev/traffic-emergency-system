package com.example.suco.repository.sos.goi.user;

import com.example.suco.model.MuaGoi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface SoHuuGoiRepository extends JpaRepository<MuaGoi, Long> {
List<MuaGoi> findByUserId(String userId);
Optional<MuaGoi> findFirstByUserIdAndTrangThai(String userId, String trangThai);
Optional<MuaGoi> findFirstByUserIdAndTrangThaiAndNgayHetHanAfter(
        String userId, String trangThai, java.time.LocalDateTime now
    );
    List<MuaGoi> findByTrangThaiAndNgayMuaBefore(
    String trangThai, LocalDateTime time
);
}
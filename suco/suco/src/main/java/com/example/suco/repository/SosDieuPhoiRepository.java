// package com.example.suco.repository;

// import com.example.suco.model.SosDieuPhoi;
// import com.example.suco.model.TruSo;

// import org.springframework.data.jpa.repository.JpaRepository;

// import java.util.List;
// import java.util.Optional;

// public interface SosDieuPhoiRepository extends JpaRepository<SosDieuPhoi, Long> {

//     List<SosDieuPhoi> findBySosIdOrderByThuTuAsc(Long sosId);

//     Optional<SosDieuPhoi> findBySosIdAndTruSoId(Long sosId, Long truSoId);

//     Optional<SosDieuPhoi> findBySosIdAndTrangThai(Long sosId, String trangThai);

//     Optional<SosDieuPhoi> findBySosIdAndThuTu(Long sosId, Integer thuTu);

//         Optional<SosDieuPhoi> findTopBySosIdOrderByThoiGianGuiDesc(Long sosId);

//         Optional<SosDieuPhoi> findBySosIdAndTruSoIdAndTrangThai(
//         Long sosId,
//         Long truSoId,
//         String trangThai
// );
// }
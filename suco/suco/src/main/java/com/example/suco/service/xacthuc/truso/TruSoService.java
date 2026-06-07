package com.example.suco.service.xacthuc.truso;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.suco.service.location.GeocodingService;
import com.example.suco.dto.vanhanh.truso.TruSoMapDto;
import com.example.suco.model.TruSo;
import com.example.suco.repository.vanhanh.TruSoRepository;

import ch.hsr.geohash.GeoHash;

@Service
public class TruSoService {

    @Autowired
    private TruSoRepository truSoRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private GeocodingService geocodingService;


    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public TruSo saveTruSo(TruSo truSo) {
        String gh = GeoHash.withCharacterPrecision(truSo.getViDo(), truSo.getKinhDo(), 6).toBase32();
        truSo.setGeohash(gh);
        truSo.setDiaChi(
            geocodingService.getAddress(
                truSo.getViDo(),
                truSo.getKinhDo()
            )
        );


        // ================= VALIDATE USERNAME =================
String username = truSo.getTenDangNhap();

if (username == null 
    || username.length() < 5 
    || username.length() > 20 
    || username.contains(" ")) {
    throw new RuntimeException("Tên đăng nhập phải 5-20 ký tự, không chứa khoảng trắng");
}

// CHECK TRÙNG USERNAME
boolean isDuplicate = truSoRepository.existsByTenDangNhap(username);

if (truSo.getId() == null) {
    // CREATE
    if (isDuplicate) {
        throw new RuntimeException("Tên đăng nhập đã tồn tại");
    }
} else {
    // UPDATE
    TruSo existing = truSoRepository.findById(truSo.getId())
        .orElseThrow(() -> new RuntimeException("Không tìm thấy trụ sở"));

    if (!existing.getTenDangNhap().equals(username) && isDuplicate) {
        throw new RuntimeException("Tên đăng nhập đã tồn tại");
    }
}

// ================= VALIDATE PASSWORD =================
String password = truSo.getMatKhau();
String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$";

if (truSo.getId() == null || (password != null && !password.isBlank())) {
    if (password == null || !password.matches(regex)) {
        throw new RuntimeException("Mật khẩu phải lớn hơn hoặc bằng 8 ký tự, gồm hoa, thường, số và ký tự đặc biệt");
    }
}
        TruSo saved;
        if (truSo.getId() != null) {
            saved = truSoRepository.findById(truSo.getId())
                    .map(existing -> {
                        existing.setKinhDo(truSo.getKinhDo());
                        existing.setViDo(truSo.getViDo());
                        existing.setGeohash(gh);
                        if (truSo.getTenTruSo() != null) existing.setTenTruSo(truSo.getTenTruSo());
                        // Chỉ mã hóa nếu mật khẩu mới KHÁC với mật khẩu đã lưu (tức là mật khẩu thô mới)
                        if (truSo.getMatKhau() != null && !truSo.getMatKhau().isBlank() 
                            && !truSo.getMatKhau().equals(existing.getMatKhau())) {
                            existing.setMatKhau(passwordEncoder.encode(truSo.getMatKhau()));
                        }
                        return truSoRepository.save(existing);
                    })
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy trụ sở ID: " + truSo.getId()));
        } else {
            if (truSo.getMatKhau() != null && !truSo.getMatKhau().isBlank()) {
                truSo.setMatKhau(passwordEncoder.encode(truSo.getMatKhau()));
            }
            saved = truSoRepository.save(truSo);
        }

        messagingTemplate.convertAndSend("/topic/tru-so", new TruSoMapDto(saved.getId(), saved.getTenTruSo(), saved.getKinhDo(), saved.getViDo(), saved.getDiaChi()));
        return saved;
    }

    public List<TruSoMapDto> getAllTruSoForMap() {
        return truSoRepository.findAll().stream()
                .map(ts -> new TruSoMapDto(ts.getId(), ts.getTenTruSo(), ts.getKinhDo(), ts.getViDo(), ts.getDiaChi()))
                .collect(Collectors.toList());
    }

    @Transactional
public void deleteTruSo(Long id) {
    TruSo ts = truSoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Trụ sở không tồn tại"));

    truSoRepository.delete(ts);
    messagingTemplate.convertAndSend("/topic/tru-so-delete", id);
}



    private double tinhKhoangCach(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public TruSo timTruSoTheoId(Long idTruSo) {
        return truSoRepository.findById(idTruSo).orElse(null);
    }

    public List<TruSo> layTatCaTruSo() {
        return truSoRepository.findAll();
    }
}
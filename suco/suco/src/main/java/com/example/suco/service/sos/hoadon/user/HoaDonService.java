package com.example.suco.service.sos.hoadon.user;

import com.example.suco.dto.sos.hoadon.quanly.HoaDonUserResponseDTO;
import com.example.suco.mapper.HoaDonCuuHoMapper;
import com.example.suco.model.HoaDon;
import com.example.suco.model.TruSo;
import com.example.suco.repository.sos.hoadon.HoaDonCuuHoRepository;
import com.example.suco.repository.vanhanh.TruSoRepository;
import com.example.suco.repository.vanhanh.UserRepository;
import com.example.suco.service.xacthuc.user.token.FirebaseService;
import com.google.firebase.auth.FirebaseAuthException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HoaDonService {

    @Autowired
    private HoaDonCuuHoRepository hoaDonRepository;

    @Autowired
    private HoaDonCuuHoMapper hoaDonMapper;

    @Autowired
private FirebaseService firebaseService;
@Autowired
private TruSoRepository truSoRepository;

@Autowired
private UserRepository userRepository;

    // =========================
    // LẤY DANH SÁCH HÓA ĐƠN USER
    // =========================
    public List<HoaDonUserResponseDTO> getHoaDonUser(String authHeader) {

        String uid;
try {
    uid = firebaseService.extractUid(authHeader);
} catch (FirebaseAuthException e) {
    throw new RuntimeException("Token không hợp lệ");
}

        List<HoaDon> list = hoaDonRepository
                .findByUserIdOrderByIdDesc(uid);

        List<Long> truSoIds = list.stream()
        .map(HoaDon::getTrusoId)
        .toList();

List<String> userIds = list.stream()
        .map(HoaDon::getUserId)
        .toList();

Map<Long, TruSo> truSoMap = truSoRepository.findAllById(truSoIds)
        .stream()
        .collect(Collectors.toMap(TruSo::getId, t -> t));

Map<String, User> userMap = userRepository.findAllByUid(userIds)
        .stream()
        .collect(Collectors.toMap(User::getUid, u -> u));

return list.stream()
        .map(hd -> hoaDonMapper.toUserDTO(
                hd,
                userMap.get(hd.getUserId()),
                truSoMap.get(hd.getTrusoId())
        ))
        .toList();
    }
    

    // =========================
    // CHI TIẾT HÓA ĐƠN USER
    // =========================
    public HoaDonUserResponseDTO getChiTietHoaDon(Long id, String authHeader) {

        String uid;
try {
    uid = firebaseService.extractUid(authHeader);
} catch (FirebaseAuthException e) {
    throw new RuntimeException("Token không hợp lệ");
}

        HoaDon hd = hoaDonRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy hóa đơn")
                );

        // CHECK QUYỀN USER
        if (!hd.getUserId().equals(uid)) {
            throw new RuntimeException("Không có quyền truy cập hóa đơn này");
        }

        return hoaDonMapper.toUserDTO(hd);
    }
}
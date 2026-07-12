package com.example.suco.service.tienich.tien.user;

import com.example.suco.dto.tienich.tien.quydoi.DoiTienRequestDTO;
import com.example.suco.dto.tienich.tien.quydoi.DoiTienResponseDTO;
import com.example.suco.mapper.TienMapper;
import com.example.suco.model.DoiTien;
import com.example.suco.model.User;
import com.example.suco.repository.tienich.tien.DoiTienRepository;
import com.example.suco.repository.vanhanh.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import com.example.suco.dto.tienich.tien.quanly.ThongKeQuyResponseDTO;
import com.example.suco.dto.tienich.tien.quanly.VinhDanhDTO;


@Service
public class DoiTienService {
    @Autowired private DoiTienRepository doiTienRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SimpMessagingTemplate messagingTemplate;
    @Autowired
private TienMapper tienMapper;

    private final long HE_SO = 100L; // 10 điểm * 100 = 1000 VNĐ

    @Transactional
    public boolean thucHienDoiTien(String uid, DoiTienRequestDTO dto) {
    if (dto.getSoDiemDoi() <= 0) return false;

    User user = userRepository.findById(uid).orElse(null);

    if (user == null || user.getTotalPoints() < dto.getSoDiemDoi()) return false;

    // 1. Trừ điểm
    user.setTotalPoints(user.getTotalPoints() - dto.getSoDiemDoi());
    userRepository.save(user);

    long giaTriGiaoDich = dto.getSoDiemDoi() * HE_SO;

    if ("QUYEN_GOP".equals(dto.getLoaiDoi())) {
        LocalDateTime start = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);

        DoiTien existing = doiTienRepository.findFirstByUserIdAndLoaiDoiAndNgayDoiBetween(
                uid, "QUYEN_GOP", start, end
        );

        if (existing != null) {
            existing.setSoDiemDoi(existing.getSoDiemDoi() + dto.getSoDiemDoi());

            long giaTriCu = (existing.getGiaTri() != null) ? existing.getGiaTri() : 0L;
            existing.setGiaTri(giaTriCu + giaTriGiaoDich);

            existing.setNgayDoi(LocalDateTime.now());
            doiTienRepository.save(existing);
        } else {
            saveNewRecord(uid, dto, giaTriGiaoDich);
        }

        broadcastFundStats();
    } else {
        saveNewRecord(uid, dto, giaTriGiaoDich);
    }

    messagingTemplate.convertAndSend("/topic/user-stats/" + uid, user);

    return true;
}

    private void saveNewRecord(String uid, DoiTienRequestDTO dto, long giaTri) {

    DoiTien entity = tienMapper.toEntity(dto);

    entity.setUserId(uid);
    entity.setGiaTri(giaTri);
    entity.setNgayDoi(LocalDateTime.now());

    doiTienRepository.save(entity);
}

public void broadcastFundStats() {

    Long tong = doiTienRepository.sumAllDonationValues();

    if (tong == null) {
        tong = 0L;
    }

    ThongKeQuyResponseDTO dto =
            new ThongKeQuyResponseDTO(
                    tong,
                    getBangVinhDanh()
            );

    messagingTemplate.convertAndSend(
            "/topic/public-fund",
            dto
    );
}

public List<VinhDanhDTO> getBangVinhDanh() {

    List<DoiTien> logs =
            doiTienRepository.findTop10ByLoaiDoiOrderByNgayDoiDesc("QUYEN_GOP");

    return logs.stream().map(log -> {

        User user =
                userRepository.findById(log.getUserId()).orElse(null);

        return new VinhDanhDTO(
                user != null ? user.getName() : "Ẩn danh",
                log.getGiaTri(), 
                log.getNgayDoi()
        );

    }).collect(Collectors.toList());
}
public List<DoiTienResponseDTO> getLichSu(String uid, String loai) {

    List<DoiTien> list;

    if (loai != null && !loai.isEmpty()) {
        list = doiTienRepository.findByUserIdAndLoaiDoi(uid, loai);
    } else {
        list = doiTienRepository.findByUserId(uid);
    }

    return list.stream()
            .map(tienMapper::toResponseDTO)
            .collect(Collectors.toList());
}
public List<DoiTienResponseDTO> getAllLichSu(String loai) {

    List<DoiTien> list;

    if (loai != null && !loai.isEmpty()) {
        list = doiTienRepository.findByLoaiDoi(loai.toUpperCase());
    } else {
        list = doiTienRepository.findAll();
    }

    return list.stream()
            .map(tienMapper::toResponseDTO)
            .collect(Collectors.toList());
}
public ThongKeQuyResponseDTO getThongKeQuy() {

    Long tong = doiTienRepository.sumAllDonationValues();

    if (tong == null) {
        tong = 0L;
    }

    return new ThongKeQuyResponseDTO(
            tong,
            getBangVinhDanh()
    );
}
}
package com.example.suco.service.tienich.qua.user;

import com.example.suco.dto.tienich.qua.quydoi.TuiQuaResponseDTO;
import com.example.suco.mapper.qua.DoiQuaMapper;
import com.example.suco.model.*;
import com.example.suco.repository.tienich.qua.TuiQuaRepository;
import com.example.suco.repository.tienich.qua.DoiQuaRepository;
import com.example.suco.repository.tienich.qua.QuaRepository;
import com.example.suco.repository.vanhanh.UserRepository;
import com.example.suco.dto.tienich.qua.quydoi.DoiQuaRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

@Service
public class DoiQuaService {
    @Autowired
    private DoiQuaRepository doiQuaRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private QuaRepository quaRepository;
    
    @Autowired
    private TuiQuaRepository tuiQuaRepository;

    @Autowired
    private DoiQuaMapper doiQuaMapper;

    @Transactional
    public boolean thucHienDoiQua(String uid, DoiQuaRequestDTO dto) {

          System.out.println("UID = " + uid);
    System.out.println("quaId = " + dto.getQuaId());
    System.out.println("soLuong = " + dto.getSoLuong());

        LocalDateTime now = LocalDateTime.now();

        User user = userRepository.findById(uid)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        Qua qua = quaRepository.findById(dto.getQuaId())
                .orElseThrow(() -> new RuntimeException("Quà không tồn tại"));

        // 1. Check trạng thái
        if (qua.getTrangThai() != Qua.TrangThai.HOAT_DONG) {
            throw new RuntimeException("Quà không còn khả dụng");
        }

        // 2. Check hết hạn
        if (qua.getNgayKetThuc() != null && now.isAfter(qua.getNgayKetThuc())) {
            throw new RuntimeException("Quà đã hết thời gian");
        }

        // 4. Trừ điểm
        int tongDiem = qua.getDiem() * dto.getSoLuong();

        // Check đủ điểm
        if (user.getTotalPoints() < tongDiem){
            throw new RuntimeException("Không đủ điểm để đổi số lượng này");
        }
        user.setTotalPoints(user.getTotalPoints() - tongDiem);
        userRepository.save(user);

        // 5. GỘP QUÀ
        Optional<TuiQua> existing =
                tuiQuaRepository.findByUserIdAndQuaId(uid, dto.getQuaId());

        if (existing.isPresent()) {
            TuiQua item = existing.get();
            item.setSoLuong(
                item.getSoLuong() + dto.getSoLuong()
            );
            tuiQuaRepository.save(item);

        } else {
            TuiQua newItem = new TuiQua();
            newItem.setUserId(uid);
            newItem.setQuaId(dto.getQuaId());
            newItem.setSoLuong(dto.getSoLuong());

            tuiQuaRepository.save(newItem);
        }

        DoiQua lichSu = doiQuaMapper.toDoiQuaEntity(
                dto,
                uid,
                qua.getDiem() * dto.getSoLuong()
        );

        doiQuaRepository.save(lichSu);

        return true;
    }

    public List<TuiQuaResponseDTO> getMyGifts(String uid) {
        return tuiQuaRepository.findByUserId(uid)
        .stream()
        .map(doiQuaMapper::toTuiQuaResponse)
        .toList();
    }
}
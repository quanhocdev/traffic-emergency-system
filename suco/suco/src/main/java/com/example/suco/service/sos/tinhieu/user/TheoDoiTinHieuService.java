package com.example.suco.service.sos.tinhieu.user;
import com.example.suco.dto.sos.tinhieu.TheoDoiSOSDetailResponseDTO;
import com.example.suco.mapper.TinHieuMapper;
import com.example.suco.repository.sos.tinhieu.TinHieuSOSRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TheoDoiTinHieuService {

    @Autowired
    private TinHieuSOSRepository tinHieuSOSRepository;

    @Autowired
    private TinHieuMapper tinHieuMapper;

    public List<TheoDoiSOSDetailResponseDTO> layDanhSach(String uid) {

        return tinHieuSOSRepository.findByUserUid(uid)
        .stream()
        .map(sos -> {

            String tenTruSo = "...";

            return tinHieuMapper.toTheoDoiDto(
                    sos,
                    tenTruSo
            );
        })
        .toList();
    }
}
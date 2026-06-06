package com.example.suco.service.suco.baocao.user;
import com.example.suco.dto.suco.baocao.TheoDoiSuCoDetailResponseDTO;
import com.example.suco.mapper.SuCoMapper;
import com.example.suco.repository.suco.baocao.BaoCaoSuCoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TheoDoiBaoCaoService {

    @Autowired
    private BaoCaoSuCoRepository baoCaoSuCoRepository;

    @Autowired
    private SuCoMapper suCoMapper;

    public List<TheoDoiSuCoDetailResponseDTO> layDanhSach(String uid) {

        return baoCaoSuCoRepository.findByReporterUid(uid)
                .stream()
                .map(suCoMapper::toTheoDoiDto)
                .toList();
    }
}
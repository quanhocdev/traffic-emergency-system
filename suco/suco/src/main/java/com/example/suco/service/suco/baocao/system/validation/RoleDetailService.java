package com.example.suco.service.suco.baocao.system.validation;

import com.example.suco.model.BaoCaoSuCo;
import com.example.suco.repository.suco.baocao.BaoCaoSuCoRepository;
import com.example.suco.mapper.SuCoMapper;
import org.springframework.stereotype.Service;

@Service
public class RoleDetailService {

    private final BaoCaoSuCoRepository repo;
    private final SuCoMapper mapper;

    public RoleDetailService(BaoCaoSuCoRepository repo, SuCoMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public Object getDetail(Long id, String role) {

    BaoCaoSuCo sc = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Not found"));

    switch (role) {
        case "ADMIN":
            return mapper.toAdminDetailDto(sc);

        case "TRU_SO":
            return mapper.toTruSoDetailDto(sc);

        default:
            return mapper.toUserDetailDto(sc);
    }
}
}
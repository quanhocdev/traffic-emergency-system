package com.example.suco.service.sos.tinhieu.user;
import com.example.suco.mapper.TinHieuMapper;
import com.example.suco.mapper.info.InfoUserMapper;
import com.example.suco.repository.sos.tinhieu.TinHieuSOSRepository;
import com.example.suco.service.xacthuc.truso.TruSoService;
import com.example.suco.service.sos.tinhieu.user.workflow.gui.VipService;
import com.example.suco.dto.info.truso.TruSoMapDto;
import com.example.suco.dto.info.user.UserInfoResponseDTO;
import com.example.suco.dto.sos.tinhieu.user.TheoDoiSOSDetailResponseDTO;
import com.example.suco.dto.sos.tinhieu.user.TheoDoiSOSItemResponseDTO;
import com.example.suco.mapper.info.InfoTruSoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TheoDoiTinHieuService {

    @Autowired
    private TinHieuSOSRepository tinHieuSOSRepository;

    @Autowired
    private TinHieuMapper tinHieuMapper;

    @Autowired
    private InfoUserMapper infoUserMapper;

    @Autowired
private TruSoService truSoService;


    @Autowired
    private InfoTruSoMapper infoTruSoMapper;

   public List<TheoDoiSOSItemResponseDTO> layDanhSachItem(
        String uid
) {

    return tinHieuSOSRepository.findByUserUid(uid)
            .stream()
            .map(sos -> {

                String tenTruSo = null;

                if (sos.getIdTruSoTiepNhan() != null) {

                    var truSo =
                            truSoService.timTruSoTheoId(
                                    sos.getIdTruSoTiepNhan()
                            );

                    if (truSo != null) {
                        tenTruSo = truSo.getTenTruSo();
                    }
                }

                return tinHieuMapper.toTheoDoiItemDto(
                        sos,
                        tenTruSo
                );
            })
            .toList();
}

public TheoDoiSOSDetailResponseDTO layChiTiet(Long id, String uid) {

    var sos = tinHieuSOSRepository
            .findByIdAndUserUid(id, uid)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy SOS"));

    TruSoMapDto truSoDto = null;

    if (sos.getIdTruSoTiepNhan() != null) {

        var truSo = truSoService.timTruSoTheoId(
                sos.getIdTruSoTiepNhan()
        );

        if (truSo != null) {
            truSoDto = infoTruSoMapper.toMapDto(truSo);
        }
    }

    UserInfoResponseDTO userInfo =
            infoUserMapper.toUserInfoResponseDTO(sos.getUser());

    return tinHieuMapper.toTheoDoiDto(
            sos,
            truSoDto,
            userInfo
    );
}
}
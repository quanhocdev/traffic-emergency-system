package com.example.suco.service.xacthuc.truso;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.suco.service.location.GeocodingService;
import com.example.suco.dto.info.truso.TruSoMapDto;
import com.example.suco.dto.vanhanh.truso.TruSoCreateRequestDTO;
import com.example.suco.mapper.info.InfoTruSoMapper;
import com.example.suco.model.TruSo;
import com.example.suco.repository.vanhanh.TruSoRepository;

import ch.hsr.geohash.GeoHash;

@Service
public class TruSoService {

    @Autowired
    private InfoTruSoMapper infoTruSoMapper;

    @Autowired
    private TruSoRepository truSoRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private GeocodingService geocodingService;

    @Autowired
    private ValidationInfoTruSoService validationInfoTruSoService;

    @Autowired
    private TruSoRealtimeService truSoRealtimeService;


    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
public TruSoMapDto createTruSo(TruSoCreateRequestDTO dto) {

    TruSo truSo = infoTruSoMapper.toEntity(dto);

    TruSo saved = saveTruSo(truSo);

    return infoTruSoMapper.toMapDto(saved);
}

@Transactional
public TruSo saveTruSo(TruSo truSo) {

    String gh = GeoHash.withCharacterPrecision(
            truSo.getViDo(),
            truSo.getKinhDo(),
            6
    ).toBase32();

    truSo.setGeohash(gh);

    truSo.setDiaChi(
            geocodingService.getAddress(
                    truSo.getViDo(),
                    truSo.getKinhDo()
            )
    );

    validationInfoTruSoService.validateUsername(truSo);

    TruSo saved;

    if (truSo.getId() != null) {

        saved = truSoRepository.findById(truSo.getId())
                .map(existing -> {

                    existing.setKinhDo(truSo.getKinhDo());
                    existing.setViDo(truSo.getViDo());
                    existing.setGeohash(gh);

                    if (truSo.getTenTruSo() != null) {
                        existing.setTenTruSo(truSo.getTenTruSo());
                    }

                    if (truSo.getMatKhau() != null
                            && !truSo.getMatKhau().isBlank()) {

                        existing.setMatKhau(
                                passwordEncoder.encode(
                                        truSo.getMatKhau()
                                )
                        );
                    }

                    return truSoRepository.save(existing);
                })
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy trụ sở ID: "
                                        + truSo.getId()));

    } else {

        if (truSo.getMatKhau() != null
                && !truSo.getMatKhau().isBlank()) {

            truSo.setMatKhau(
                    passwordEncoder.encode(
                            truSo.getMatKhau()
                    )
            );
        }

        saved = truSoRepository.save(truSo);
    }

    truSoRealtimeService.sendTruSoUpdated(saved);

    return saved;
}
    public List<TruSoMapDto> getAllTruSoForMap() {
        return truSoRepository.findAll().stream()
                .map(infoTruSoMapper::toMapDto)
                .collect(Collectors.toList());
    }

    @Transactional
public void deleteTruSo(Long id) {
    TruSo ts = truSoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Trụ sở không tồn tại"));

    truSoRepository.delete(ts);
    messagingTemplate.convertAndSend("/topic/tru-so-delete", id);
}

    public TruSo timTruSoTheoId(Long idTruSo) {
        return truSoRepository.findById(idTruSo).orElse(null);
    }

    public List<TruSo> layTatCaTruSo() {
        return truSoRepository.findAll();
    }
}
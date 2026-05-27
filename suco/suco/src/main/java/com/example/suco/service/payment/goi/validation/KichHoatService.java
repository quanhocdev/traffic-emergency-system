package com.example.suco.service.payment.goi.validation;

import com.example.suco.model.MuaGoi;
import com.example.suco.repository.payment.MuaGoiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class KichHoatService {

    @Autowired
    private MuaGoiRepository repo;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Scheduled(fixedRate = 60000)
    public void autoActivate() {

        LocalDateTime threshold = LocalDateTime.now().minusMinutes(1);

        List<MuaGoi> list =
                repo.findByTrangThaiAndNgayMuaBefore("PENDING", threshold);

        for (MuaGoi mg : list) {
            mg.setTrangThai("ACTIVE");
            repo.save(mg);

            messagingTemplate.convertAndSend(
                    "/topic/package-status/" + mg.getUserId(),
                    "REFRESH"
            );
        }
    }
}
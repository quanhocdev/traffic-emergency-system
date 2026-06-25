package com.example.suco.service.xacthuc.truso;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.suco.mapper.info.InfoTruSoMapper;
import com.example.suco.model.TruSo;

@Service
public class TruSoRealtimeService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private InfoTruSoMapper infoTruSoMapper;

    public void sendTruSoUpdated(TruSo truSo) {

        messagingTemplate.convertAndSend(
                "/topic/tru-so",
                infoTruSoMapper.toMapDto(truSo)
        );
    }

    public void sendTruSoDeleted(Long id) {

        messagingTemplate.convertAndSend(
                "/topic/tru-so-delete",
                id
        );
    }
}
package com.example.suco.controller.call;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.util.Map;

@Controller
public class WebRTCController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/call-signal")
    public void handleCallSignal(@Payload Map<String, Object> signal) {
        // Kiểm tra an toàn dữ liệu đầu vào
        if (signal == null || !signal.containsKey("to")) {
            System.out.println("Tín hiệu WebRTC không hợp lệ: Thiếu trường 'to'");
            return;
        }

        String toUser = String.valueOf(signal.get("to"));
        String type = String.valueOf(signal.get("type"));
        String destination;

        // Logic phân luồng hòm thư
        if (toUser.startsWith("TRU_SO")) {
            // Gửi cho Web Admin (Trụ sở)
            destination = "/topic/tru-so/" + toUser + "/call";
        } else {
            // Gửi cho ứng dụng Android
            destination = "/topic/user/" + toUser + "/call";
        }

        try {
            messagingTemplate.convertAndSend(destination, signal);
            System.out.println("Forwarding [" + type + "] signal to: " + destination);
        } catch (Exception e) {
            System.err.println("Lỗi khi chuyển tiếp tín hiệu WebRTC: " + e.getMessage());
        }
    }
}


package com.example.suco.controller.sos.system;

import com.example.suco.service.dieuphoi.retry.RetryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tin-hieu-sos")
@CrossOrigin(origins = "*")
public class DieuPhoiController {

    @Autowired
    private RetryService retryService;

    @GetMapping("/dieu-phoi/{idSos}")
    public ResponseEntity<?> layThongTinDieuPhoi(@PathVariable Long idSos) {

        RetryService.SessionState s = retryService.get(idSos);

        if (s == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> res = new HashMap<>();

        res.put("idSos", s.eventId);
        res.put("index", s.index);
        res.put("queue", s.queue);
        res.put("status", s.status);

        Long currentTruSo =
                (s.queue != null && s.index < s.queue.size())
                        ? s.queue.get(s.index)
                        : null;

        res.put("truSoHienTai", currentTruSo);

        return ResponseEntity.ok(res);
    }
}
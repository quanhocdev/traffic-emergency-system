package com.example.suco.controller.dieuphoi;

import com.example.suco.model.TinHieuSOS;
import com.example.suco.repository.sos.tinhieu.TinHieuSOSRepository;

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
    private TinHieuSOSRepository tinHieuSOSRepository;

    @GetMapping("/dieu-phoi/{idSos}")
    public ResponseEntity<?> layThongTinDieuPhoi(@PathVariable Long idSos) {

        TinHieuSOS sos = tinHieuSOSRepository.findById(idSos)
                .orElse(null);

        if (sos == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> res = new HashMap<>();

        res.put("idSos", sos.getId());
        res.put("status", sos.getTrangThai());
        res.put("truSoTiepNhan", sos.getIdTruSoTiepNhan());

        return ResponseEntity.ok(res);
    }
}
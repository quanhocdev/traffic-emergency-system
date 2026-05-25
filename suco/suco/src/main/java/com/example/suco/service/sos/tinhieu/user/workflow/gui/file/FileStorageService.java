package com.example.suco.service.sos.tinhieu.user.workflow.gui.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.suco.dto.sos.tinhieu.TinHieuSOSRequestDTO;
import com.example.suco.model.TinHieuSOS;

import java.io.IOException;
import java.nio.file.*;
import java.util.Base64;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    public String saveBase64ToFile(String base64Data, String prefix) {
        try {
            if (base64Data == null || base64Data.isEmpty()) {
                return null;
            }

            String extension = prefix.contains("audio") ? ".m4a" : ".jpg";
            String fileName = System.currentTimeMillis() + "_" + prefix + extension;

            Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads", "sos");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String base64Content = base64Data.contains(",")
                    ? base64Data.split(",")[1]
                    : base64Data;

            byte[] bytes = Base64.getDecoder().decode(base64Content);

            Files.write(uploadPath.resolve(fileName), bytes);

            return "/uploads/sos/" + fileName;

        } catch (IOException e) {
            log.error("Lỗi lưu file SOS: {}", e.getMessage());
            return null;
        }
    }
    public void handleFiles(TinHieuSOS sos, TinHieuSOSRequestDTO dto) {
    if (dto.getHinhAnhBase64() != null) {
        sos.setHinhAnh(saveBase64ToFile(dto.getHinhAnhBase64(), "sos_img"));
    }

    if (dto.getGhiAmBase64() != null) {
        sos.setGhiAm(saveBase64ToFile(dto.getGhiAmBase64(), "sos_audio"));
    }
}
}
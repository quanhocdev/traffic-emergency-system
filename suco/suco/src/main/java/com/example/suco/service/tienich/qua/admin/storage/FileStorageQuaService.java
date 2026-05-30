package com.example.suco.service.tienich.qua.admin.storage;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageQuaService {

    private final String uploadDir =
            System.getProperty("user.dir") + "/uploads/";

    public String saveFile(MultipartFile file) {

        try {

            String fileName =
                    UUID.randomUUID()
                            + "_"
                            + file.getOriginalFilename();

            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            try (var inputStream = file.getInputStream()) {

                Files.copy(
                        inputStream,
                        uploadPath.resolve(fileName),
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            return fileName;

        } catch (IOException e) {

            throw new RuntimeException(
                    "Không thể lưu ảnh quà", e);
        }
    }
}
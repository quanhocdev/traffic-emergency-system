package com.example.suco.service.suco.baocao.system.file;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Base64;

@Service
public class LocalImageStorageService implements ImageStorageService {

    @Override
    public String saveBase64(String base64, String folder, String prefix, String extension) {

        try {
            String fileName = System.currentTimeMillis() + "_" + prefix + extension;

            Path uploadPath = Paths.get(
                    System.getProperty("user.dir"),
                    "uploads",
                    folder
            );

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String cleanBase64 = base64.contains(",")
                    ? base64.split(",")[1]
                    : base64;

            byte[] bytes = Base64.getDecoder().decode(cleanBase64);

            Files.write(uploadPath.resolve(fileName), bytes);

            return "/uploads/" + folder + "/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("Lỗi lưu file", e);
        }
    }

    @Override
    public String saveMultipart(MultipartFile file, String folder) {

        try {
            String filename =
                    System.currentTimeMillis() + "_" + file.getOriginalFilename();

            Path uploadDir = Paths.get(
                    System.getProperty("user.dir"),
                    "uploads",
                    folder
            );

            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Files.copy(
                    file.getInputStream(),
                    uploadDir.resolve(filename),
                    StandardCopyOption.REPLACE_EXISTING
            );

            return "/uploads/" + folder + "/" + filename;

        } catch (IOException e) {
            throw new RuntimeException("Lỗi lưu multipart file", e);
        }
    }
}
package com.example.suco.service.suco.baocao.system.file;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {

    String saveBase64(String base64, String folder, String prefix, String extension);

    String saveMultipart(MultipartFile file, String folder);
}
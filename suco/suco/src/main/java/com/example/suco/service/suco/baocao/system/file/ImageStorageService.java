package com.example.suco.service.suco.baocao.system.file;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {

    String saveBase64Image(String base64);

    String saveMultipartImage(MultipartFile file);
}
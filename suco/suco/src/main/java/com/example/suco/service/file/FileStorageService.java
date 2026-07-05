package com.example.suco.service.file;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    // base64 có thể là ảnh, ghi âm; folder là thư mục con trong uploads;
    // prefix là tên tên file tránh trùng; extension là đuôi file
    String saveBase64(String base64, String folder, String prefix, String extension);

    // MultipartFile là ảnh, folder là thư mục con trong uploads. Admin chỉ cần gọi saveMultipart(image, "reports") là được
    String saveMultipart(MultipartFile file, String folder);
}
// package com.example.suco.service;

// import com.example.suco.dto.TinHieuSOSRequestDTO;
// import com.example.suco.model.MuaGoi;
// import com.example.suco.model.TinHieuSOS;
// import com.example.suco.model.TruSo;
// import com.example.suco.repository.MuaGoiRepository;
// import com.example.suco.repository.TinHieuSOSRepository;
// import com.example.suco.service.DieuPhoiSOSService.ThongTinDieuPhoi;
// import com.example.suco.util.GeocodingUtil;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.messaging.simp.SimpMessagingTemplate;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
// import org.springframework.web.server.ResponseStatusException;
// import java.io.IOException;
// import java.nio.file.*;
// import java.util.*;
// import com.example.suco.service.sos.system.mapper.*;

// import org.springframework.http.HttpStatus;
// @Service
// public class TinHieuSOSService {

//     private static final Logger log = LoggerFactory.getLogger(TinHieuSOSService.class);

//     @Autowired
//     private SimpMessagingTemplate messagingTemplate;

//     @Autowired
//     private TinHieuSOSRepository tinHieuSOSRepository;

//     @Autowired
//     private MuaGoiRepository muaGoiRepository;

//     @Autowired
//     private TruSoService truSoService;

//     @Autowired
//     private GeocodingUtil geocodingUtil;

//     @Autowired
//     private DieuPhoiSOSService dieuPhoiService;

//     @Autowired
//     private TinHieuMapper tinHieuMapper;

   
//     private String saveBase64ToFile(String base64Data, String prefix) {
//         try {
//             String extension = prefix.contains("audio") ? ".m4a" : ".jpg";
//             String fileName = System.currentTimeMillis() + "_" + prefix + extension;
            
//             Path uploadPath = Paths.get(System.getProperty("user.dir"), "uploads", "sos");
//             if (!Files.exists(uploadPath)) {
//                 Files.createDirectories(uploadPath);
//             }

//             String base64Content = base64Data.contains(",") ? base64Data.split(",")[1] : base64Data;
//             byte[] bytes = Base64.getDecoder().decode(base64Content);
            
//             Files.write(uploadPath.resolve(fileName), bytes);
//             return "/uploads/sos/" + fileName;
//         } catch (IOException e) {
//             log.error("Lỗi lưu file SOS: {}", e.getMessage());
//             return null;
//         }
//     }
    
// }
package com.example.suco.controller.xacthuc.user;

import com.example.suco.dto.xacthuc.user.AuthRequest;
import com.example.suco.model.User;
import com.example.suco.repository.vanhanh.UserRepository;
import com.example.suco.service.vanhanh.user.UserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {



    private final UserRepository userRepository;
    private final UserService userService; 

    public AuthController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

@GetMapping("/all-users")
public ResponseEntity<?> getAllUsersForTest() {
    try {
        // Gọi service để lấy toàn bộ danh sách
        return ResponseEntity.ok(userService.getAllUsers());
    } catch (Exception e) {
        return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
    }
}
    @PostMapping("/sync")
public ResponseEntity<?> sync(@RequestBody AuthRequest request) {
    try {
        String uid;
        String email;
        String name;

            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(request.getToken());
            uid = decodedToken.getUid();
            email = decodedToken.getEmail();
            name = (String) decodedToken.getClaims().get("name");

        User user = userRepository.findById(uid).orElse(new User());
        
        user.setUid(uid);
        user.setEmail(email);
        user.setName(name);
        user.setProvider("google");

        userRepository.save(user);

        // Trả về thông tin đầy đủ
        User userWithPackage = userService.getUserInfo(uid);
        return ResponseEntity.ok(userWithPackage);

    } catch (Exception e) {
        return ResponseEntity.status(401).body("Invalid Token: " + e.getMessage());
    }
}

        
@GetMapping("/me")
public ResponseEntity<?> getMyInfo(@RequestHeader("Authorization") String authHeader) {
    try {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(
                Map.of("error", "MISSING_TOKEN", "message", "Thiếu token")
            );
        }

        String token = authHeader.substring(7);

        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
        String uid = decodedToken.getUid();

        User user = userService.getUserInfo(uid);

        if (user == null) {
            return ResponseEntity.status(404).body(
                Map.of("error", "USER_NOT_FOUND", "message", "Không tìm thấy user")
            );
        }

        return ResponseEntity.ok(user);

    } catch (Exception e) {
        return ResponseEntity.status(401).body(
            Map.of("error", "INVALID_TOKEN", "message", e.getMessage())
        );
    }
}

     private boolean isValidUid(String uid) {
        return uid != null && uid.length() <= 256 && uid.matches("^[A-Za-z0-9_-]+$");
    }
    
}

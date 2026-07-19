package com.example.suco.controller.xacthuc.admin;

import com.example.suco.model.User;
import com.example.suco.repository.vanhanh.UserRepository;
import com.example.suco.security.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminAuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenProvider tokenProvider;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody Map<String, String> req) {

        String email = req.get("email");
        String password = req.get("password");

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Sai tài khoản hoặc mật khẩu"));
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("message", "Sai tài khoản hoặc mật khẩu"));
        }

        if (!"ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(403).body(Map.of("message", "Không có quyền truy cập vùng quản trị"));
        }

        String token = tokenProvider.generateToken(user.getUid(), user.getRole());
        
        // Đồng bộ thời gian sống cookie bằng giây
        long maxAgeSeconds = jwtExpirationMs / 1000;

        ResponseCookie cookie = ResponseCookie.from("accessToken_admin", token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(maxAgeSeconds)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("token", token, "message", "Đăng nhập admin thành công"));
    }

    @GetMapping("/login")
    public String loginPage() {
        return "admin/login";
    }
   
}
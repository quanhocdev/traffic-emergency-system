package com.example.suco.controller.xacthuc.truso;

import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.example.suco.config.AppConfig;
import com.example.suco.model.TruSo;
import com.example.suco.repository.sos.tinhieu.TinHieuSOSRepository;
import com.example.suco.repository.vanhanh.TruSoRepository;
import com.example.suco.security.TokenProvider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import java.util.Map;

@Controller
@RequestMapping("/truso")
public class LoginController {

    private final TruSoRepository truSoRepository;
    private final TinHieuSOSRepository tinHieuSOSRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final AppConfig appConfig;
    private final TokenProvider tokenProvider;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    public LoginController(TruSoRepository truSoRepository,
                           TinHieuSOSRepository tinHieuSOSRepository,
                           AppConfig appConfig,
                           TokenProvider tokenProvider) {
        this.truSoRepository = truSoRepository;
        this.tinHieuSOSRepository = tinHieuSOSRepository;
        this.appConfig = appConfig;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestParam String username,
                                   @RequestParam String password) {

        Optional<TruSo> truSo = truSoRepository.findByTenDangNhap(username);

        if (truSo.isPresent() && passwordEncoder.matches(password, truSo.get().getMatKhau())) {
            TruSo t = truSo.get();

            // Tạo mã JWT Token với vai trò TRUSO giống như cách cấp cho ADMIN
            String token = tokenProvider.generateToken(String.valueOf(t.getId()), "TRUSO");
            
            // Đồng bộ thời gian sống cho cookie bằng giây
            long maxAgeSeconds = jwtExpirationMs / 1000;

            // Đóng gói JWT vào cookie HttpOnly "accessToken" để trình duyệt tự quản lý
            ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                    .httpOnly(true)
                    .secure(false) // Đổi thành true khi chạy trên môi trường HTTPS thực tế
                    .path("/truso")
                    .sameSite("Lax")
                    .maxAge(maxAgeSeconds)
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(Map.of(
                            "message", "Login success",
                            "id", t.getId(),
                            "tenTruSo", t.getTenTruSo(),
                            "tenDangNhap", t.getTenDangNhap()
                    ));
        }

        return ResponseEntity.status(401).body(Map.of(
                "message", "Sai tài khoản hoặc mật khẩu"
        ));
    }
}
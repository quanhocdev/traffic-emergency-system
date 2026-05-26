package com.example.suco.controller.xacthuc.truso;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.example.suco.config.AppConfig;
import com.example.suco.model.TruSo;
import com.example.suco.repository.TruSoRepository;
import com.example.suco.repository.sos.tinhieu.TinHieuSOSRepository;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.suco.service.xacthuc.truso.TruSoConfigService;

@Controller
@RequestMapping("/truso")
public class LoginController {

    private final TruSoRepository truSoRepository;
    private final TinHieuSOSRepository tinHieuSOSRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final AppConfig appConfig;

    public LoginController(TruSoRepository truSoRepository,
                             TinHieuSOSRepository tinHieuSOSRepository,
                             AppConfig appConfig) {
    this.truSoRepository = truSoRepository;
    this.tinHieuSOSRepository = tinHieuSOSRepository;
    this.appConfig = appConfig;
}

    @PostMapping("/login")
@ResponseBody
public ResponseEntity<?> login(@RequestParam String username,
                               @RequestParam String password,
                               HttpSession session) {

    Optional<TruSo> truSo = truSoRepository.findByTenDangNhap(username);

    if (truSo.isPresent() && passwordEncoder.matches(password, truSo.get().getMatKhau())) {

        session.setAttribute("currentTruSo", truSo.get());

        TruSo t = truSo.get();
         session.setAttribute("currentTruSo", t);

    var auth = new UsernamePasswordAuthenticationToken(
                t, // principal
                null,
                List.of() 
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        return ResponseEntity.ok(Map.of(
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
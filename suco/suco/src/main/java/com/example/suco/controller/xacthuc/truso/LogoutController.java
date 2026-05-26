package com.example.suco.controller.xacthuc.truso;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import jakarta.servlet.http.Cookie;

@Controller
@RequestMapping("/truso")
public class LogoutController {

@PostMapping("/logout")
@ResponseBody
public ResponseEntity<?> logout(HttpSession session, HttpServletResponse response) {

    session.invalidate();

    Cookie cookie = new Cookie("JSESSIONID", null);
    cookie.setMaxAge(0);
    cookie.setPath("/");
    response.addCookie(cookie);

    return ResponseEntity.ok(Map.of("message", "Logout success"));
}
}
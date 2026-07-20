// package com.example.suco.controller.xacthuc.truso;

// import org.springframework.http.ResponseEntity;
// import org.springframework.stereotype.Controller;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.ResponseBody;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.ResponseCookie;
// import java.util.Map;

// @Controller
// @RequestMapping("/truso")
// public class LogoutController {

//     @PostMapping("/logout")
//     @ResponseBody
//     public ResponseEntity<?> logout() {

//         // Ghi đè cookie cũ với thời gian sống bằng 0 để xóa sạch token ở client
//         ResponseCookie cookie = ResponseCookie.from("accessToken_truso", "")
//                 .httpOnly(true)
//                 .secure(false)
//                 .path("/")
//                 .sameSite("Lax")
//                 .maxAge(0)
//                 .build();

//         return ResponseEntity.ok()
//                 .header(HttpHeaders.SET_COOKIE, cookie.toString())
//                 .body(Map.of("message", "Logout success"));
//     }
// }